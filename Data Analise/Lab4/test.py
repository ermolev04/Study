from __future__ import annotations
from pathlib import Path

import re
import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
import csv

from sklearn.model_selection import StratifiedKFold, cross_val_score
from sklearn.pipeline import Pipeline
from sklearn.feature_extraction.text import CountVectorizer

from sklearn.metrics import (
    f1_score, make_scorer,
    adjusted_rand_score, normalized_mutual_info_score,
    silhouette_score
)

from sklearn.linear_model import LogisticRegression
from sklearn.svm import LinearSVC
from sklearn.naive_bayes import MultinomialNB

from sklearn.feature_selection import (
    chi2,
    SelectKBest,
    SelectFromModel,
    RFECV,
)

from sklearn.base import clone
from sklearn.cluster import KMeans
from sklearn.decomposition import TruncatedSVD
from sklearn.manifold import TSNE


RANDOM_STATE = 52
N_TOP_WORDS = 30

FS_K = 2000
SFS_CANDIDATES = 100
SFS_SELECT = 150


def clean_text_basic(s: str) -> str:
    s = s.lower()
    s = re.sub(r"[^a-z0-9\s']", " ", s)
    s = re.sub(r"\s+", " ", s).strip()
    return s


def top_feature_names(vectorizer, selector, top_n=30):
    feature_names = np.array(vectorizer.get_feature_names_out())

    if hasattr(selector, "scores_") and getattr(selector, "scores_", None) is not None:
        scores = np.asarray(selector.scores_)
        idx = np.argsort(scores)[::-1][:top_n]
        return list(feature_names[idx])

    if hasattr(selector, "importances_") and getattr(selector, "importances_", None) is not None:
        imp = np.asarray(selector.importances_)
        idx = np.argsort(imp)[::-1][:top_n]
        return list(feature_names[idx])

    if hasattr(selector, "ranking_") and getattr(selector, "ranking_", None) is not None:
        ranking = np.asarray(selector.ranking_)
        idx = np.argsort(ranking)[:top_n]
        return list(feature_names[idx])

    if hasattr(selector, "get_support"):
        mask = selector.get_support()
        selected = feature_names[mask]
        return list(selected[:top_n])

    return list(feature_names[:top_n])

def print_common_words(top_words_by_method: dict[str, list[str]], top_n: int = 30):
    print("\n" + "=" * 80)
    print(f"COMMON WORDS BETWEEN FS METHODS (using Top-{top_n})")
    print("=" * 80)

    sets = {k: set(v) for k, v in top_words_by_method.items()}
    names = list(sets.keys())

    print("\n--- Pairwise intersections ---")
    for i in range(len(names)):
        for j in range(i + 1, len(names)):
            a, b = names[i], names[j]
            inter = sorted(sets[a] & sets[b])
            print(f"\n[{a}] ∩ [{b}] -> {len(inter)} words")
            if inter:
                print(inter)

    print("\n--- Intersection of ALL methods ---")
    all_inter = sorted(set.intersection(*sets.values())) if sets else []
    print(f"ALL -> {len(all_inter)} words")
    if all_inter:
        print(all_inter)

    print("\n--- Words appearing in >= 3 methods ---")
    counts: dict[str, int] = {}
    for s in sets.values():
        for w in s:
            counts[w] = counts.get(w, 0) + 1
    common3 = sorted([w for w, c in counts.items() if c >= 3], key=lambda w: (-counts[w], w))
    print(f">=3 methods -> {len(common3)} words")
    if common3:
        print([(w, counts[w]) for w in common3])




def evaluate_classifiers(X_text, y, feature_selector=None, selector_name="BASE"):
    cv = StratifiedKFold(n_splits=5, shuffle=True, random_state=RANDOM_STATE)
    scorer = make_scorer(f1_score, pos_label=1)

    vect = CountVectorizer(
        preprocessor=clean_text_basic,
        ngram_range=(1, 1),
        min_df=2
    )

    classifiers = {
        "LogReg(L2)": LogisticRegression(max_iter=3000, solver="liblinear", random_state=RANDOM_STATE),
        "LinearSVC": LinearSVC(random_state=RANDOM_STATE),
        "MultinomialNB": MultinomialNB()
    }

    results = {}
    for name, clf in classifiers.items():
        steps = [("vect", vect)]
        if feature_selector is not None:
            steps.append(("fs", feature_selector))
        steps.append(("clf", clf))
        pipe = Pipeline(steps)

        scores = cross_val_score(pipe, X_text, y, cv=cv, scoring=scorer, n_jobs=-1)
        results[name] = (scores.mean(), scores.std())

    print(f"\n\Classification quality (F1, 5-fold CV) [{selector_name}]")
    for k, (m, s) in results.items():
        print(f"{k:15s}: mean={m:.4f}, std={s:.4f}")
    return results


def evaluate_classifiers_on_matrix(X, y, feature_selector=None, selector_name="BASE (MATRIX)"):
    cv = StratifiedKFold(n_splits=5, shuffle=True, random_state=RANDOM_STATE)

    classifiers = {
        "LogReg(L2)": LogisticRegression(max_iter=3000, solver="liblinear", random_state=RANDOM_STATE),
        "LinearSVC": LinearSVC(random_state=RANDOM_STATE),
        "MultinomialNB": MultinomialNB()
    }

    print(f"\nClassification quality (F1, 5-fold CV) [{selector_name}]")
    results = {}

    for clf_name, clf in classifiers.items():
        fold_scores = []
        for train_idx, test_idx in cv.split(X, y):
            X_tr, X_te = X[train_idx], X[test_idx]
            y_tr, y_te = y[train_idx], y[test_idx]

            if feature_selector is not None:
                fs = feature_selector
                fs.fit(X_tr, y_tr)
                X_tr = fs.transform(X_tr)
                X_te = fs.transform(X_te)

            clf.fit(X_tr, y_tr)
            y_pred = clf.predict(X_te)
            fold_scores.append(f1_score(y_te, y_pred, pos_label=1))

        results[clf_name] = (float(np.mean(fold_scores)), float(np.std(fold_scores)))
        m, s = results[clf_name]
        print(f"{clf_name:15s}: mean={m:.4f}, std={s:.4f}")

    return results


def clustering_quality(X, y, title):
    k = len(np.unique(y))
    km = KMeans(n_clusters=k, n_init="auto", random_state=RANDOM_STATE)
    labels = km.fit_predict(X)

    ari = adjusted_rand_score(y, labels)
    nmi = normalized_mutual_info_score(y, labels)
    sil = silhouette_score(X, labels, metric="cosine")

    print(f"\nClustering quality [{title}]")
    print(f"ARI={ari:.4f} (external), NMI={nmi:.4f} (external), silhouette(cosine)={sil:.4f} (internal)")
    return labels, (ari, nmi, sil)


def visualize_pca_tsne_save_png(
    X, y_true, y_cluster,
    title_prefix: str,
    out_dir: str = "results"
):
    out_path = Path(out_dir)
    out_path.mkdir(parents=True, exist_ok=True)

    def save_scatter(Z, c, title, filename):
        fig, ax = plt.subplots(figsize=(8, 6), dpi=150)
        ax.scatter(Z[:, 0], Z[:, 1], c=c, s=10)
        ax.set_title(title)
        ax.set_xlabel("x")
        ax.set_ylabel("y")
        fig.tight_layout()
        fig.savefig(out_path / filename, bbox_inches="tight")
        plt.close(fig)

    svd2 = TruncatedSVD(n_components=2, random_state=RANDOM_STATE)
    X2 = svd2.fit_transform(X)

    safe_prefix = re.sub(r"[^a-zA-Z0-9_\-]+", "_", title_prefix)

    save_scatter(
        X2, y_true,
        f"{title_prefix} - SVD(PCA) 2D (TRUE class)",
        f"{safe_prefix}_svd_true.png"
    )

    save_scatter(
        X2, y_cluster,
        f"{title_prefix} - SVD(PCA) 2D (CLUSTER label)",
        f"{safe_prefix}_svd_cluster.png"
    )

    svd50 = TruncatedSVD(n_components=50, random_state=RANDOM_STATE)
    X50 = svd50.fit_transform(X)

    tsne = TSNE(
        n_components=2,
        perplexity=30,
        init="pca",
        learning_rate="auto",
        random_state=RANDOM_STATE
    )
    Xt = tsne.fit_transform(X50)

    save_scatter(
        Xt, y_true,
        f"{title_prefix} - tSNE 2D (TRUE class)",
        f"{safe_prefix}_tsne_true.png"
    )

    save_scatter(
        Xt, y_cluster,
        f"{title_prefix} - tSNE 2D (CLUSTER label)",
        f"{safe_prefix}_tsne_cluster.png"
    )

    print(f"[OK] Saved results to: {out_path.resolve()}")

PATH = "SMS.tsv"

df = pd.read_csv(
    PATH,
    sep="\t",
    header=0,
    names=["class", "text"],
    dtype=str,
    encoding="utf-8",
    engine="python",
    quoting=csv.QUOTE_NONE,
    escapechar="\\",
)

df = df.dropna(subset=["class", "text"]).reset_index(drop=True)

df["class"] = df["class"].astype(str).str.lower().str.strip()
y = (df["class"] == "spam").astype(int).to_numpy()
X_text = df["text"].astype(str).to_numpy()

print("Dataset size:", df.shape)
print("Class balance:", df["class"].value_counts().to_dict())


class EuclidDistanceTopK:
    def __init__(self, k=FS_K):
        self.k = k
        self.support_mask_ = None
        self.scores_ = None

    def fit(self, X, y):
        y = np.asarray(y)
        spam_idx = (y == 1)
        ham_idx = (y == 0)

        mu_spam = np.asarray(X[spam_idx].mean(axis=0)).ravel()
        mu_ham  = np.asarray(X[ham_idx].mean(axis=0)).ravel()

        scores = np.abs(mu_spam - mu_ham)
        self.scores_ = scores

        idx = np.argsort(scores)[::-1][:self.k]
        mask = np.zeros(X.shape[1], dtype=bool)
        mask[idx] = True
        self.support_mask_ = mask
        return self

    def transform(self, X):
        return X[:, self.support_mask_]

    def fit_transform(self, X, y):
        return self.fit(X, y).transform(X)

    def get_support(self):
        return self.support_mask_


class PermutationImportanceTopK:
    def __init__(
        self,
        k=FS_K,
        candidate_k=None,
        estimator=None,
        n_repeats=1,
        random_state=RANDOM_STATE,
    ):
        self.k = int(k)
        self.candidate_k = int(candidate_k) if candidate_k is not None else None
        self.estimator = estimator if estimator is not None else LinearSVC(random_state=random_state)
        self.n_repeats = int(n_repeats)
        self.random_state = int(random_state)

        self.support_mask_ = None
        self.importances_ = None

    @staticmethod
    def _euclid_scores(X, y):
        y = np.asarray(y)
        spam_idx = (y == 1)
        ham_idx = (y == 0)

        mu_spam = np.asarray(X[spam_idx].mean(axis=0)).ravel()
        mu_ham = np.asarray(X[ham_idx].mean(axis=0)).ravel()
        return np.abs(mu_spam - mu_ham)

    def fit(self, X, y):
        y = np.asarray(y)
        n_features = X.shape[1]

        cand_k = self.candidate_k
        if cand_k is None:
            cand_k = min(max(self.k, 800), n_features)
        else:
            cand_k = min(max(self.k, cand_k), n_features)

        fast_scores = self._euclid_scores(X, y)
        candidates = np.argsort(fast_scores)[::-1][:cand_k]

        Xc = X[:, candidates].toarray()

        est = clone(self.estimator)
        est.fit(Xc, y)

        y_pred = est.predict(Xc)
        baseline = f1_score(y, y_pred, pos_label=1)

        rng = np.random.RandomState(self.random_state)

        importances_c = np.zeros(Xc.shape[1], dtype=float)

        for j in range(Xc.shape[1]):
            drops = []
            for _ in range(self.n_repeats):
                Xp = Xc.copy()
                perm = rng.permutation(Xp.shape[0])
                Xp[:, j] = Xp[perm, j]

                yp = est.predict(Xp)
                score_p = f1_score(y, yp, pos_label=1)
                drops.append(baseline - score_p)

            importances_c[j] = float(np.mean(drops))

        importances_full = np.zeros(n_features, dtype=float)
        importances_full[candidates] = importances_c
        self.importances_ = importances_full

        idx = np.argsort(importances_full)[::-1][:self.k]
        mask = np.zeros(n_features, dtype=bool)
        mask[idx] = True
        self.support_mask_ = mask
        return self

    def transform(self, X):
        return X[:, self.support_mask_]

    def fit_transform(self, X, y):
        return self.fit(X, y).transform(X)

    def get_support(self):
        return self.support_mask_



class SFSWrapper:
    def __init__(
        self,
        n_features_to_select=SFS_SELECT,
        candidate_indices=None,
        base_estimator=None,
        cv_splits=3
    ):
        self.n_features_to_select = n_features_to_select
        self.candidate_indices = candidate_indices
        self.base_estimator = base_estimator or LinearSVC(random_state=RANDOM_STATE)
        self.cv = StratifiedKFold(n_splits=cv_splits, shuffle=True, random_state=RANDOM_STATE)

        self.support_mask_ = None
        self.selected_indices_ = None
        self.scores_history_ = []

    def fit(self, X, y):
        y = np.asarray(y)

        if self.candidate_indices is None:
            candidates = np.arange(X.shape[1])
        else:
            candidates = np.asarray(self.candidate_indices, dtype=int)

        selected = []
        best_score = -1.0

        for _ in range(self.n_features_to_select):
            best_feat = None
            best_feat_score = best_score

            for feat in candidates:
                if feat in selected:
                    continue
                trial = selected + [int(feat)]
                X_trial = X[:, trial]

                scores = cross_val_score(
                    self.base_estimator, X_trial, y,
                    cv=self.cv,
                    scoring=make_scorer(f1_score, pos_label=1),
                    n_jobs=-1
                )
                m = float(scores.mean())
                if m > best_feat_score:
                    best_feat_score = m
                    best_feat = int(feat)

            if best_feat is None:
                break

            selected.append(best_feat)
            best_score = best_feat_score
            self.scores_history_.append(best_score)

        self.selected_indices_ = np.array(selected, dtype=int)
        mask = np.zeros(X.shape[1], dtype=bool)
        mask[self.selected_indices_] = True
        self.support_mask_ = mask
        return self

    def transform(self, X):
        return X[:, self.selected_indices_]

    def fit_transform(self, X, y):
        return self.fit(X, y).transform(X)

    def get_support(self):
        return self.support_mask_

filter_lib = SelectKBest(score_func=chi2, k=FS_K)

embedded_lib = SelectFromModel(
    LinearSVC(penalty="l1", dual=False, random_state=RANDOM_STATE, max_iter=5000),
    max_features=FS_K
)

wrapper_lib = RFECV(
    estimator=LinearSVC(random_state=RANDOM_STATE),
    step=0.2,
    cv=StratifiedKFold(n_splits=3, shuffle=True, random_state=RANDOM_STATE),
    scoring=make_scorer(f1_score, pos_label=1),
    min_features_to_select=500
)


vect0 = CountVectorizer(preprocessor=clean_text_basic, min_df=2)
X0 = vect0.fit_transform(X_text)

filter_custom = EuclidDistanceTopK(k=FS_K)
embedded_custom = PermutationImportanceTopK(
    k=FS_K,
    candidate_k=800,
    estimator=LinearSVC(random_state=RANDOM_STATE),
    n_repeats=1,
    random_state=RANDOM_STATE
)


filter_custom.fit(X0, y)
candidate_idx = np.argsort(filter_custom.scores_)[::-1][:SFS_CANDIDATES]

wrapper_custom = SFSWrapper(
    n_features_to_select=SFS_SELECT,
    candidate_indices=candidate_idx,
    base_estimator=LinearSVC(random_state=RANDOM_STATE),
    cv_splits=3
)

top_words_by_method = {}

for sel, name in [
    (filter_custom, "FILTER(custom) Euclid distance"),
    (filter_lib, "FILTER(lib) chi2"),
    (embedded_custom, "EMBEDDED(custom) Permutation Importance (PI)"),
    (embedded_lib, "EMBEDDED(lib) L1-SVM (SelectFromModel)"),
    (wrapper_custom, "WRAPPER(custom) SFS"),
    (wrapper_lib, "WRAPPER(lib) SVM-RFE (RFECV)"),
]:
    sel.fit(X0, y)
    words = top_feature_names(vect0, sel, top_n=N_TOP_WORDS)
    top_words_by_method[name] = words
    print(f"\nTop-{N_TOP_WORDS} words: {name}\n{words}")

print_common_words(top_words_by_method, top_n=N_TOP_WORDS)


base = evaluate_classifiers(X_text, y, feature_selector=None, selector_name="NO FS")

res_filter_custom = evaluate_classifiers(X_text, y, feature_selector=filter_custom, selector_name="FILTER custom (euclid)")
res_filter_lib    = evaluate_classifiers(X_text, y, feature_selector=filter_lib,    selector_name="FILTER lib (chi2)")

res_emb_custom    = evaluate_classifiers(X_text, y, feature_selector=embedded_custom, selector_name="EMBEDDED custom (RandomForest)")
res_emb_lib       = evaluate_classifiers(X_text, y, feature_selector=embedded_lib, selector_name="EMBEDDED lib (L1-SVM)")

vect_fixed = CountVectorizer(preprocessor=clean_text_basic, min_df=2)
X_fixed = vect_fixed.fit_transform(X_text)

tmp_filter = EuclidDistanceTopK(k=FS_K)
tmp_filter.fit(X_fixed, y)
candidate_idx_fixed = np.argsort(tmp_filter.scores_)[::-1][:SFS_CANDIDATES]

wrapper_custom = SFSWrapper(
    n_features_to_select=SFS_SELECT,
    candidate_indices=candidate_idx_fixed,
    base_estimator=LinearSVC(random_state=RANDOM_STATE),
    cv_splits=3
)


res_wrap_custom = evaluate_classifiers_on_matrix(
    X_fixed, y, feature_selector=wrapper_custom, selector_name="WRAPPER custom (SFS) [fixed vocab]"
)

res_wrap_lib      = evaluate_classifiers(X_text, y, feature_selector=wrapper_lib,    selector_name="WRAPPER lib (SVM-RFE)")


def pick_best_method(results_dict_by_method):
    target_clf = "LinearSVC"
    best_name, best_score = None, -1

    for method_name, res in results_dict_by_method.items():
        if method_name == "NO FS":
            continue

        m, _ = res[target_clf]
        if m > best_score:
            best_score = m
            best_name = method_name

    return best_name, best_score


methods_results = {
    "NO FS": base,
    "FILTER custom (euclid)": res_filter_custom,
    "FILTER lib (chi2)": res_filter_lib,
    "EMBEDDED custom (RandomForest)": res_emb_custom,
    "EMBEDDED lib (L1-SVM)": res_emb_lib,
    "WRAPPER custom (SFS)": res_wrap_custom,
    "WRAPPER lib (SVM-RFE)": res_wrap_lib,
}

best_method, best_score = pick_best_method(methods_results)
print(f"\n>>> Selected feature selection method for next stages: {best_method} (LinearSVC mean F1={best_score:.4f})")

chosen_selector = {
    "NO FS": None,
    "FILTER custom (euclid)": filter_custom,
    "FILTER lib (chi2)": filter_lib,
    "EMBEDDED custom (RandomForest)": embedded_custom,
    "EMBEDDED lib (SVM-RFE)": embedded_lib,
    "WRAPPER custom (SFS)": wrapper_custom,
    "WRAPPER lib (SVM-RFE)": wrapper_lib,
}[best_method]

vect = CountVectorizer(preprocessor=clean_text_basic, min_df=2)
X_full = vect.fit_transform(X_text)

if chosen_selector is None:
    X_sel = X_full
else:
    chosen_selector.fit(X_full, y)
    X_sel = chosen_selector.transform(X_full)

clusters_before, metrics_before = clustering_quality(X_full, y, "BEFORE feature selection")
clusters_after,  metrics_after  = clustering_quality(X_sel,  y, "AFTER feature selection")

visualize_pca_tsne_save_png(X_full, y_true=y, y_cluster=clusters_before, title_prefix="BEFORE_FS", out_dir="results")
visualize_pca_tsne_save_png(X_sel,  y_true=y, y_cluster=clusters_after,  title_prefix="AFTER_FS",  out_dir="results")
