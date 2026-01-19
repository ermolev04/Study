import os
import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
from sklearn.preprocessing import StandardScaler, OneHotEncoder
from sklearn.linear_model import Lasso, Ridge
from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score
import itertools
import warnings
warnings.filterwarnings("ignore")
np.random.seed(52)

INPUT_CSV = "data_cleaned.csv"
RESULTS_DIR = "results"
RANDOM_STATE = 52
TEST_SIZE = 0.2

GRID = {
    "loss_type": ['piecewise', 'logistic', 'exponential'],
    "lr": [0.001, 0.01, 0.05],
    "lambda1": [0.05, 0.001, 0.01],
    "lambda2": [0.05, 0.001, 0.01]
}

GD_EPOCHS = 10000

def ensure_dir(d):
    os.makedirs(d, exist_ok=True)

def moving_average(x, w):
    if len(x) < w: return x
    return np.convolve(x, np.ones(w)/w, mode='valid')

if not os.path.exists(INPUT_CSV):
    raise FileNotFoundError(f"Не найден {INPUT_CSV} в текущей папке")

df = pd.read_csv(INPUT_CSV)
ensure_dir(RESULTS_DIR)

thr = df['winrate'].median()
y_bin = (df['winrate'] > thr).astype(int)
y_pm = np.where(y_bin==1, 1, -1)
X_df = df.drop(columns=['winrate'])

assert all(pd.api.types.is_numeric_dtype(X_df[c]) for c in X_df.columns)

X = X_df.values.astype(float)
y = y_pm.copy()
y_bin = np.where(y==1, 1, 0)

scaler = StandardScaler()
X_scaled = scaler.fit_transform(X)

X_train, X_test, y_train_pm, y_test_pm = train_test_split(X_scaled, y, test_size=TEST_SIZE,
                                                         random_state=RANDOM_STATE, stratify=y)
y_train_bin = np.where(y_train_pm==1, 1, 0)
y_test_bin = np.where(y_test_pm==1, 1, 0)

def ridge_closed_form(F, y_pm, tau=1.0):
    n, m = F.shape
    X = np.hstack([np.ones((n,1)), F])
    I = np.eye(m+1)
    I[0,0] = 0.0
    theta_bias = np.linalg.inv(X.T @ X + tau * I) @ (X.T @ y_pm)
    bias = theta_bias[0]
    theta = theta_bias[1:]
    return theta, bias

tau_ridge = 1.0
theta_ridge, bias_ridge = ridge_closed_form(X_train, y_train_pm, tau=tau_ridge)
preds_ridge = np.sign(X_test @ theta_ridge + bias_ridge)
acc_ridge = accuracy_score(y_test_bin, np.where(preds_ridge==1,1,0))

def compute_margin(F, y_pm, theta, bias):
    return y_pm * (F @ theta + bias)

def loss_and_grad(F, y_pm, theta, bias, loss_type='piecewise', lambda1=0.0, lambda2=0.0):
    n = F.shape[0]
    m = compute_margin(F, y_pm, theta, bias)
    if loss_type == 'piecewise':
        loss_vec = np.maximum(0.0, 1.0 - m)
        loss = loss_vec.mean()
        indicator = (m < 1.0).astype(float)
        grad_theta = - ((indicator * y_pm) @ F) / n
        grad_bias = - (indicator * y_pm).sum() / n
    elif loss_type == 'logistic':
        loss_vec = np.log1p(np.exp(-m))
        loss = loss_vec.mean()
        sigma = 1.0 / (1.0 + np.exp(m))
        grad_theta = - ((sigma * y_pm) @ F) / n
        grad_bias = - (sigma * y_pm).sum() / n
    elif loss_type == 'exponential':
        loss_vec = np.exp(-m)
        loss = loss_vec.mean()
        grad_theta = - ((loss_vec * y_pm) @ F) / n
        grad_bias = - (loss_vec * y_pm).sum() / n
    else:
        raise ValueError("Unknown loss_type")

    grad_theta = grad_theta + 2.0 * lambda2 * theta + lambda1 * np.sign(theta)
    reg_loss = lambda1 * np.linalg.norm(theta, 1) + lambda2 * np.linalg.norm(theta, 2)**2
    return loss + reg_loss, grad_theta, grad_bias

def train_gd(F, y_pm, loss_type='piecewise', lr=0.01, epochs=1000, lambda1=0.0, lambda2=0.0, record_every=10):
    n, d = F.shape
    theta = np.zeros(d)
    bias = 0.0
    history = {"iter": [], "loss": [], "theta": [], "bias": []}
    for it in range(1, epochs+1):
        loss_val, grad_theta, grad_bias = loss_and_grad(F, y_pm, theta, bias, loss_type=loss_type, lambda1=lambda1, lambda2=lambda2)
        theta = theta - lr * grad_theta
        bias = bias - lr * grad_bias
        if (it % record_every == 0) or (it == 1) or (it == epochs):
            history["iter"].append(it)
            history["loss"].append(loss_val)
            history["theta"].append(theta.copy())
            history["bias"].append(bias)
    return theta, bias, history

best = {"acc": -1.0}
total_combinations = len(GRID['loss_type']) * len(GRID['lr']) * len(GRID['lambda1']) * len(GRID['lambda2'])
for loss_type, lr, l1, l2 in itertools.product(GRID['loss_type'], GRID['lr'], GRID['lambda1'], GRID['lambda2']):
    theta_gd_tmp, bias_gd_tmp, _ = train_gd(X_train, y_train_pm, loss_type=loss_type, lr=lr, epochs=500, lambda1=l1, lambda2=l2, record_every=500)
    preds_tmp = np.sign(X_test @ theta_gd_tmp + bias_gd_tmp)
    acc_tmp = accuracy_score(y_test_bin, np.where(preds_tmp==1,1,0))
    if acc_tmp > best["acc"]:
        best = {"acc": acc_tmp, "loss_type": loss_type, "lr": lr, "l1": l1, "l2": l2, "theta": theta_gd_tmp.copy(), "bias": bias_gd_tmp}

theta_best, bias_best, history_best = train_gd(X_train, y_train_pm, loss_type=best["loss_type"],
                                       lr=best["lr"], epochs=GD_EPOCHS, lambda1=best["l1"], lambda2=best["l2"],
                                       record_every=1)

loss_arr = np.array(history_best["loss"])
iters = np.array(history_best["iter"])

# 1) Эмпирический риск на трейне
plt.figure(figsize=(8,4))
plt.plot(iters, loss_arr, label='train risk')
plt.title(f"Train empirical risk (raw) — {best['loss_type']}")
plt.xlabel("iteration")
plt.ylabel("empirical risk (loss + reg)")
plt.grid(True)
plt.tight_layout()
plt.savefig(os.path.join(RESULTS_DIR, "train_empirical_risk_raw.png"))
plt.close()

# 1) Точность от итераций (train & test)
acc_iter_train, acc_iter_test = [], []
for th, bs in zip(history_best["theta"], history_best["bias"]):
    # train
    preds_tr = np.sign(X_train @ th + bs)
    acc_iter_train.append(accuracy_score(y_train_bin, (preds_tr == 1).astype(int)))
    # test
    preds_te = np.sign(X_test @ th + bs)
    acc_iter_test.append(accuracy_score(y_test_bin, (preds_te == 1).astype(int)))

plt.figure(figsize=(8,4))
plt.plot(iters, acc_iter_train, label="Train accuracy")
plt.plot(iters, acc_iter_test,  label="Test accuracy")
plt.title("Accuracy vs iterations")
plt.xlabel("iteration")
plt.ylabel("accuracy")
plt.ylim(0.0, 1.0)
plt.grid(True)
plt.legend()
plt.tight_layout()
plt.savefig(os.path.join(RESULTS_DIR, "accuracy_vs_iterations.png"))
plt.close()

train_fracs = np.linspace(0.1, 0.9, 9)
n_repeats = 10
test_means, test_lows, test_highs = [], [], []

for frac in train_fracs:
    accs = []
    for r in range(n_repeats):
        X_tr, X_te, y_tr, y_te = train_test_split(
            X_scaled, y, train_size=frac, stratify=y, random_state=RANDOM_STATE + r
        )
        theta_tmp, bias_tmp, _ = train_gd(
            X_tr, y_tr,
            loss_type=best["loss_type"], lr=best["lr"],
            epochs=500, lambda1=best["l1"], lambda2=best["l2"],
            record_every=500
        )
        preds_tmp = np.sign(X_te @ theta_tmp + bias_tmp)
        accs.append(accuracy_score((y_te == 1).astype(int), (preds_tmp == 1).astype(int)))
    accs = np.array(accs)
    mean = accs.mean()
    sem = accs.std(ddof=1) / np.sqrt(n_repeats)
    ci = 1.96 * sem
    test_means.append(mean)
    test_lows.append(mean - ci)
    test_highs.append(mean + ci)

ridge_test_acc = acc_ridge

alphas = np.logspace(-4, 1, 25)
coefs_l1, coefs_l2 = [], []
for a in alphas:
    lasso = Lasso(alpha=a, max_iter=10000)
    lasso.fit(X_scaled, (y == 1).astype(int))
    coefs_l1.append(lasso.coef_.copy())
    ridge = Ridge(alpha=a)
    ridge.fit(X_scaled, (y == 1).astype(int))
    coefs_l2.append(ridge.coef_.copy())
coefs_l1 = np.array(coefs_l1)
coefs_l2 = np.array(coefs_l2)

ensure_dir(RESULTS_DIR)

# 2) Кривая обучения + 95% CI + ridge горизонтальная линия
plt.figure(figsize=(8,4))
plt.plot(train_fracs, test_means, marker='o', label='GD classifier (mean acc)')
plt.fill_between(train_fracs, test_lows, test_highs, alpha=0.25)
plt.hlines(ridge_test_acc, xmin=train_fracs[0], xmax=train_fracs[-1],
           linestyles='--', label=f'Ridge closed-form acc (alpha={tau_ridge})')
plt.title(f"Test accuracy vs train fraction\n(best GD: loss={best['loss_type']}, lr={best['lr']}, l1={best['l1']}, l2={best['l2']})")
plt.xlabel("Train fraction")
plt.ylabel("Accuracy")
plt.ylim(0.0, 1.0)
plt.legend()
plt.grid(True)
plt.tight_layout()
plt.savefig(os.path.join(RESULTS_DIR, "test_accuracy_vs_train_fraction.png"))
plt.close()

# 3) График коэффициентов для L1 (Lasso)
plt.figure(figsize=(9,6))
for j in range(coefs_l1.shape[1]):
    plt.plot(np.log10(alphas), coefs_l1[:, j], label=X_df.columns[j] if j < 12 else None)
plt.title("Coefficient paths vs log10(L1 alpha) — Lasso (L1 only)")
plt.xlabel("log10(alpha)")
plt.ylabel("coefficient value")
plt.grid(True)
plt.legend(bbox_to_anchor=(1.05, 1), loc='upper left', fontsize='small')
plt.tight_layout()
plt.savefig(os.path.join(RESULTS_DIR, "coef_path_l1.png"))
plt.close()

# 4) График коэффициентов для L2 (Ridge)
plt.figure(figsize=(9,6))
for j in range(coefs_l2.shape[1]):
    plt.plot(np.log10(alphas), coefs_l2[:, j], label=X_df.columns[j] if j < 12 else None)
plt.title("Coefficient paths vs log10(L2 alpha) — Ridge (L2 only)")
plt.xlabel("log10(alpha)")
plt.ylabel("coefficient value")
plt.grid(True)
plt.legend(bbox_to_anchor=(1.05, 1), loc='upper left', fontsize='small')
plt.tight_layout()
plt.savefig(os.path.join(RESULTS_DIR, "coef_path_l2.png"))
plt.close()