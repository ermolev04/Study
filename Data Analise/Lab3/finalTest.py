import os
import warnings

import numpy as np
import pandas as pd
import matplotlib.pyplot as plt

from sklearn.preprocessing import StandardScaler
from sklearn.metrics import mean_absolute_error, mean_squared_error

from statsmodels.tsa.seasonal import STL
from statsmodels.tsa.stattools import adfuller, acf
from statsmodels.graphics.tsaplots import plot_acf
from statsmodels.tsa.holtwinters import ExponentialSmoothing

from prophet import Prophet

warnings.filterwarnings("ignore")
np.random.seed(52)

BTC_CSV = "btc_1d_data_2018_to_2025.csv"
RESULTS_DIR = "results"
TRAIN_FRACTION = 0.8

GRID_REG = {
    "lr":      [0.001, 0.01, 0.05],
    "lambda1": [0.0,   0.001, 0.01],
    "lambda2": [0.0,   0.001, 0.01]
}
GD_EPOCHS_SEARCH = 1000
GD_EPOCHS_FINAL  = 5000
GD_RECORD_EVERY  = 50

def ensure_dir(d):
    os.makedirs(d, exist_ok=True)

def regression_metrics(y_true, y_pred, return_all=False):
    y_true = np.asarray(y_true)
    y_pred = np.asarray(y_pred)
    mse = mean_squared_error(y_true, y_pred)
    rmse = np.sqrt(mse)
    if not return_all:
        return rmse
    mae = mean_absolute_error(y_true, y_pred)
    return {"MAE": mae, "MSE": mse, "RMSE": rmse}

def adf_test(series):
    result = adfuller(series.dropna())
    print(f"p-value = {result[1]:.6f}")
    if result[1] < 0.05:
        print("Вывод: ряд можно считать стационарным (p-value < 0.05)")
    else:
        print("Вывод: ряд НЕстационарный (p-value ≥ 0.05)")

def invert_log_diff(last_log_value, diffs):
    log_values = [last_log_value]
    for d in diffs:
        log_values.append(log_values[-1] + d)
    log_values = np.array(log_values[1:])
    return np.exp(log_values)

def reconstruct_prices_from_diffs(dates, diffs_pred, ts_daily):
    first_idx = dates[0]
    pos = ts_daily.index.get_loc(first_idx)
    base_log = ts_daily['log_close'].iloc[pos - 1]
    return invert_log_diff(base_log, diffs_pred)

def save_residuals_acf(residuals, title, filename, lags=60):
    residuals = pd.Series(residuals).dropna().values
    plt.figure(figsize=(10, 4))
    plot_acf(residuals, lags=lags, zero=False)
    plt.title(title)
    plt.grid(True)
    plt.tight_layout()
    plt.savefig(os.path.join(RESULTS_DIR, filename))
    plt.close()

def print_all_model_metrics(metrics_dict):
    dfm = pd.DataFrame(metrics_dict).T
    dfm = dfm[["MAE", "RMSE"]]
    print("\nМетрики (на тесте, в ценах Close)")
    print(dfm.to_string(float_format=lambda x: f"{x:,.4f}"))

if not os.path.exists(BTC_CSV):
    raise FileNotFoundError(f"Файл {BTC_CSV} не найден в текущей папке")

ensure_dir(RESULTS_DIR)

df_raw = pd.read_csv(BTC_CSV, parse_dates=['Open time'])
df_raw = df_raw.set_index('Open time').sort_index()

ts_daily = df_raw[['Close']].copy()
ts_daily = ts_daily.asfreq('D')
ts_daily['Close'] = ts_daily['Close'].ffill()

plt.figure(figsize=(12, 4))
plt.plot(ts_daily.index, ts_daily['Close'])
plt.title('BTC – дневные цены закрытия')
plt.xlabel('Дата')
plt.ylabel('Цена, USD')
plt.grid(True)
plt.tight_layout()
plt.savefig(os.path.join(RESULTS_DIR, "step0_raw_series.png"))
plt.close()

stl = STL(ts_daily['Close'], period=365, robust=True)
stl_res = stl.fit()

fig = stl_res.plot()
fig.set_size_inches(12, 8)
plt.tight_layout()
plt.savefig(os.path.join(RESULTS_DIR, "step1_stl_decomposition.png"))
plt.close()

ts_daily['log_close'] = np.log(ts_daily['Close'])

plt.figure(figsize=(12, 4))
plt.plot(ts_daily.index, ts_daily['log_close'])
plt.title('Логарифм цены BTC: log(Close)')
plt.xlabel('Дата')
plt.ylabel('log(Цена)')
plt.grid(True)
plt.tight_layout()
plt.savefig(os.path.join(RESULTS_DIR, "step2_log_series.png"))
plt.close()

ts_daily['log_diff'] = ts_daily['log_close'].diff()
ts_diff = ts_daily['log_diff'].dropna()

plt.figure(figsize=(12, 4))
plt.plot(ts_diff.index, ts_diff)
plt.title('Первая разность логарифма цены BTC: Δlog(Close)')
plt.xlabel('Дата')
plt.ylabel('Δlog(Цена)')
plt.grid(True)
plt.tight_layout()
plt.savefig(os.path.join(RESULTS_DIR, "step2_log_diff_series.png"))
plt.close()

print("Close")
adf_test(ts_daily['Close'])
print("log(Close)")
adf_test(ts_daily['log_close'])
print("Δlog(Close)")
adf_test(ts_daily['log_diff'])

last_log = ts_daily['log_close'].iloc[-6]
example_diffs = ts_daily['log_diff'].iloc[-5:]
reconstructed_prices = invert_log_diff(last_log, example_diffs.values)

df_ts = ts_daily[['Close', 'log_diff']].copy()

df_ts['dayofweek']  = df_ts.index.dayofweek
df_ts['dayofmonth'] = df_ts.index.day
df_ts['dayofyear']  = df_ts.index.dayofyear
df_ts['weekofyear'] = df_ts.index.isocalendar().week.astype(int)
df_ts['month']      = df_ts.index.month
df_ts['year']       = df_ts.index.year

df_ts['dow_norm']   = df_ts['dayofweek'] / 7.0
df_ts['month_norm'] = df_ts['month'] / 12.0
df_ts['doy_norm']   = df_ts['dayofyear'] / 365.0

df_ts['dow_sin']   = np.sin(2 * np.pi * df_ts['dow_norm'])
df_ts['dow_cos']   = np.cos(2 * np.pi * df_ts['dow_norm'])
df_ts['month_sin'] = np.sin(2 * np.pi * df_ts['month_norm'])
df_ts['month_cos'] = np.cos(2 * np.pi * df_ts['month_norm'])
df_ts['doy_sin']   = np.sin(2 * np.pi * df_ts['doy_norm'])
df_ts['doy_cos']   = np.cos(2 * np.pi * df_ts['doy_norm'])

df_ts['lag1'] = df_ts['Close'].shift(1)
df_ts['lag7'] = df_ts['Close'].shift(7)

df_ts = df_ts.dropna()

feature_cols_ts = [
    'dayofweek', 'dayofmonth', 'dayofyear', 'weekofyear', 'month', 'year',
    'dow_sin', 'dow_cos', 'month_sin', 'month_cos', 'doy_sin', 'doy_cos',
    'lag1', 'lag7'
]

X_ts = df_ts[feature_cols_ts].values.astype(float)
y_diff = df_ts['log_diff'].values.astype(float)
y_price = df_ts['Close'].values.astype(float)

scaler_ts = StandardScaler()
X_ts_scaled = scaler_ts.fit_transform(X_ts)

n = len(df_ts)
split_idx_ts = int(n * TRAIN_FRACTION)

X_train_ts = X_ts_scaled[:split_idx_ts]
X_test_ts  = X_ts_scaled[split_idx_ts:]

dates_train_ts = df_ts.index[:split_idx_ts]
dates_test_ts  = df_ts.index[split_idx_ts:]

y_train_diff = y_diff[:split_idx_ts]
y_test_diff  = y_diff[split_idx_ts:]

y_train_price = y_price[:split_idx_ts]
y_test_price  = y_price[split_idx_ts:]

def loss_and_grad_reg(F, y, w, b, lambda1=0.0, lambda2=0.0):
    n, d = F.shape
    preds = F @ w + b
    residuals = preds - y

    mse = 0.5 * np.mean(residuals ** 2)

    reg_l1 = lambda1 * np.linalg.norm(w, 1)
    reg_l2 = lambda2 * np.linalg.norm(w, 2) ** 2
    loss = mse + reg_l1 + reg_l2

    grad_w = (F.T @ residuals) / n
    grad_b = residuals.mean()

    grad_w += lambda1 * np.sign(w) + 2.0 * lambda2 * w

    return loss, grad_w, grad_b

def train_gd_reg(F, y, lr=0.01, epochs=1000,
                 lambda1=0.0, lambda2=0.0,
                 record_every=50):
    n, d = F.shape
    w = np.zeros(d)
    b = 0.0
    history = {"iter": [], "loss": [], "w": [], "b": []}

    for it in range(1, epochs + 1):
        loss_val, grad_w, grad_b = loss_and_grad_reg(
            F, y, w, b,
            lambda1=lambda1, lambda2=lambda2
        )
        w = w - lr * grad_w
        b = b - grad_b * lr

        if (it % record_every == 0) or (it == 1) or (it == epochs):
            history["iter"].append(it)
            history["loss"].append(loss_val)
            history["w"].append(w.copy())
            history["b"].append(b)

    return w, b, history

best_reg = {"rmse": np.inf}
for lr in GRID_REG["lr"]:
    for l1 in GRID_REG["lambda1"]:
        for l2 in GRID_REG["lambda2"]:
            w_tmp, b_tmp, _ = train_gd_reg(
                X_train_ts, y_train_diff,
                lr=lr,
                epochs=GD_EPOCHS_SEARCH,
                lambda1=l1,
                lambda2=l2,
                record_every=GD_EPOCHS_SEARCH
            )
            y_pred_diff_tmp = X_test_ts @ w_tmp + b_tmp
            y_pred_price_tmp = reconstruct_prices_from_diffs(
                dates_test_ts, y_pred_diff_tmp, ts_daily
            )
            rmse_tmp = regression_metrics(y_test_price, y_pred_price_tmp)
            if rmse_tmp < best_reg["rmse"]:
                best_reg = {
                    "rmse": rmse_tmp,
                    "lr": lr,
                    "lambda1": l1,
                    "lambda2": l2,
                }

print("\nЛучшие гиперпараметры GD-регрессии:")
print(best_reg)

w_reg, b_reg, history_reg = train_gd_reg(
    X_train_ts, y_train_diff,
    lr=best_reg["lr"],
    epochs=GD_EPOCHS_FINAL,
    lambda1=best_reg["lambda1"],
    lambda2=best_reg["lambda2"],
    record_every=GD_RECORD_EVERY
)

y_pred_train_diff = X_train_ts @ w_reg + b_reg
y_pred_test_diff  = X_test_ts  @ w_reg + b_reg

y_pred_train_reg = reconstruct_prices_from_diffs(
    dates_train_ts, y_pred_train_diff, ts_daily
)
y_pred_test_reg = reconstruct_prices_from_diffs(
    dates_test_ts, y_pred_test_diff, ts_daily
)

loss_arr_reg = np.array(history_reg["loss"])
iters_reg    = np.array(history_reg["iter"])

plt.figure(figsize=(8, 4))
plt.plot(iters_reg, loss_arr_reg)
plt.title("GD-regression (на Δlog): train loss (MSE+reg) vs iterations")
plt.xlabel("iteration")
plt.ylabel("loss")
plt.grid(True)
plt.tight_layout()
plt.savefig(os.path.join(RESULTS_DIR, "step4_gd_reg_train_loss.png"))
plt.close()

plt.figure(figsize=(12, 4))
plt.plot(dates_train_ts, y_train_price, label='Train real')
plt.plot(dates_train_ts, y_pred_train_reg, label='Train pred (GD)', alpha=0.7)
plt.plot(dates_test_ts, y_test_price, label='Test real')
plt.plot(dates_test_ts, y_pred_test_reg, label='Test pred (GD)', alpha=0.7)
plt.axvline(dates_test_ts[0], color='k', linestyle='--', label='Test start')
plt.title("BTC regression – GD модель (обучение на Δlog, график в ценах)")
plt.xlabel("Дата")
plt.ylabel("Цена, USD")
plt.legend()
plt.grid(True)
plt.tight_layout()
plt.savefig(os.path.join(RESULTS_DIR, "step4_gd_reg_pred_vs_real.png"))
plt.close()

residuals_test_reg = y_test_price - y_pred_test_reg

plt.figure(figsize=(12, 3))
plt.plot(dates_test_ts, residuals_test_reg)
plt.axhline(0, color='k', linestyle='--')
plt.title("BTC regression (Δlog) – остатки на тесте в ценах")
plt.xlabel("Дата")
plt.ylabel("Ошибка (y - ŷ)")
plt.grid(True)
plt.tight_layout()
plt.savefig(os.path.join(RESULTS_DIR, "step4_gd_reg_residuals_test.png"))
plt.close()

save_residuals_acf(
    residuals_test_reg,
    "ACF остатков: GD regression (Δlog→price) – test",
    "acf_step4_gd_reg_residuals_test.png",
    lags=60
)

best_reg_price = {"rmse": np.inf}
for lr in GRID_REG["lr"]:
    for l1 in GRID_REG["lambda1"]:
        for l2 in GRID_REG["lambda2"]:
            w_tmp, b_tmp, _ = train_gd_reg(
                X_train_ts, y_train_price,
                lr=lr,
                epochs=GD_EPOCHS_SEARCH,
                lambda1=l1,
                lambda2=l2,
                record_every=GD_EPOCHS_SEARCH
            )
            y_pred_price_tmp = X_test_ts @ w_tmp + b_tmp
            rmse_tmp = regression_metrics(y_test_price, y_pred_price_tmp)
            if rmse_tmp < best_reg_price["rmse"]:
                best_reg_price = {
                    "rmse": rmse_tmp,
                    "lr": lr,
                    "lambda1": l1,
                    "lambda2": l2,
                    "w": w_tmp.copy(),
                    "b": b_tmp
                }

w_reg_price, b_reg_price, history_reg_price = train_gd_reg(
    X_train_ts, y_train_price,
    lr=best_reg_price["lr"],
    epochs=GD_EPOCHS_FINAL,
    lambda1=best_reg_price["lambda1"],
    lambda2=best_reg_price["lambda2"],
    record_every=GD_RECORD_EVERY
)

y_pred_train_reg_price = X_train_ts @ w_reg_price + b_reg_price
y_pred_test_reg_price  = X_test_ts  @ w_reg_price + b_reg_price

loss_arr_reg_price = np.array(history_reg_price["loss"])
iters_reg_price    = np.array(history_reg_price["iter"])

plt.figure(figsize=(8, 4))
plt.plot(iters_reg_price, loss_arr_reg_price)
plt.title("GD-regression (по Close): train loss (MSE+reg) vs iterations")
plt.xlabel("iteration")
plt.ylabel("loss")
plt.grid(True)
plt.tight_layout()
plt.savefig(os.path.join(RESULTS_DIR, "step4b_gd_price_train_loss.png"))
plt.close()

plt.figure(figsize=(12, 4))
plt.plot(dates_train_ts, y_train_price, label='Train real')
plt.plot(dates_train_ts, y_pred_train_reg_price,
         label='Train pred (GD price)', alpha=0.7)
plt.plot(dates_test_ts, y_test_price, label='Test real')
plt.plot(dates_test_ts, y_pred_test_reg_price,
         label='Test pred (GD price)', alpha=0.7)
plt.axvline(dates_test_ts[0], color='k', linestyle='--', label='Test start')
plt.title("BTC regression – GD модель по исходным ценам Close")
plt.xlabel("Дата")
plt.ylabel("Цена, USD")
plt.legend()
plt.grid(True)
plt.tight_layout()
plt.savefig(os.path.join(RESULTS_DIR, "step4b_gd_price_pred_vs_real.png"))
plt.close()

residuals_test_reg_price = y_test_price - y_pred_test_reg_price

plt.figure(figsize=(12, 3))
plt.plot(dates_test_ts, residuals_test_reg_price)
plt.axhline(0, color='k', linestyle='--')
plt.title("BTC regression (по Close) – остатки на тесте")
plt.xlabel("Дата")
plt.ylabel("Ошибка (y - ŷ)")
plt.grid(True)
plt.tight_layout()
plt.savefig(os.path.join(RESULTS_DIR, "step4b_gd_price_residuals_test.png"))
plt.close()

save_residuals_acf(
    residuals_test_reg_price,
    "ACF остатков: GD regression (Close) – test",
    "acf_step4b_gd_price_residuals_test.png",
    lags=60
)

diff_series = ts_daily['log_diff'].dropna()
close_for_diff = ts_daily['Close'].loc[diff_series.index]

n_diff = len(diff_series)
split_idx_naive = int(n_diff * TRAIN_FRACTION)

diff_train = diff_series.iloc[:split_idx_naive]
diff_test  = diff_series.iloc[split_idx_naive:]

close_train_naive = close_for_diff.iloc[:split_idx_naive]
close_test_naive  = close_for_diff.iloc[split_idx_naive:]

dates_train_naive = diff_train.index
dates_test_naive  = diff_test.index

mean_diff = diff_train.mean()
pred_diff_train_naive = np.repeat(mean_diff, len(diff_train))
pred_diff_test_naive  = np.repeat(mean_diff, len(diff_test))

y_pred_naive_train = reconstruct_prices_from_diffs(
    dates_train_naive, pred_diff_train_naive, ts_daily
)
y_pred_naive_test = reconstruct_prices_from_diffs(
    dates_test_naive, pred_diff_test_naive, ts_daily
)

series = ts_daily['Close'].dropna()
n_total = len(series)
split_idx_hw = int(n_total * TRAIN_FRACTION)

train_hw = series.iloc[:split_idx_hw]
test_hw  = series.iloc[split_idx_hw:]

dates_train_hw = train_hw.index
dates_test_hw  = test_hw.index

hw_model = ExponentialSmoothing(
    train_hw,
    trend='add',
    seasonal='add',
    seasonal_periods=365
).fit(optimized=True)

y_pred_hw_train = hw_model.fittedvalues
y_pred_hw_test  = hw_model.forecast(len(test_hw))

df_prophet = ts_daily[['Close']].reset_index()
df_prophet.columns = ['ds', 'y']

if df_prophet['ds'].dt.tz is not None:
    df_prophet['ds'] = df_prophet['ds'].dt.tz_convert(None)

df_prophet_train = df_prophet.iloc[:split_idx_hw].copy()
df_prophet_test  = df_prophet.iloc[split_idx_hw:].copy()

prophet_model = Prophet(
    yearly_seasonality=True,
    weekly_seasonality=False,
    daily_seasonality=False,
    seasonality_mode='additive'
)

prophet_model.fit(df_prophet_train)

future = prophet_model.make_future_dataframe(periods=len(df_prophet_test))
forecast = prophet_model.predict(future)

forecast_test = forecast.iloc[-len(df_prophet_test):].copy()

y_pred_prophet_test = pd.Series(
    forecast_test['yhat'].values,
    index=df_prophet_test['ds']
)

forecast_train = forecast.iloc[:len(df_prophet_train)].copy()
y_pred_prophet_train = pd.Series(
    forecast_train['yhat'].values,
    index=df_prophet_train['ds']
)

plt.figure(figsize=(12, 5))

plt.plot(dates_train_hw, train_hw.values, label='Train (real)')
plt.plot(dates_train_hw, y_pred_hw_train, label='Holt-Winters train', alpha=0.7)
plt.plot(y_pred_prophet_train.index, y_pred_prophet_train.values,
         label='Prophet train', alpha=0.7)

plt.plot(dates_test_hw, test_hw.values, label='Test (real)')
plt.plot(dates_test_hw, y_pred_hw_test, label='Holt-Winters test', alpha=0.7)
plt.plot(y_pred_prophet_test.index, y_pred_prophet_test.values, label='Prophet test', alpha=0.7)

plt.plot(dates_train_naive, y_pred_naive_train, label='Naive train (Δlog→price)', alpha=0.7)
plt.plot(dates_test_naive, y_pred_naive_test, label='Naive test (Δlog→price)', alpha=0.7)

plt.axvline(dates_test_hw[0], color='k', linestyle='--', label='Test start (HW/Prophet)')

plt.title("BTC – Naive (Δlog), Holt-Winters, Prophet (предсказания vs реальные)")
plt.xlabel("Дата")
plt.ylabel("Цена, USD")
plt.legend()
plt.grid(True)
plt.tight_layout()
plt.savefig(os.path.join(RESULTS_DIR, "step5_models_pred_vs_real.png"))
plt.close()

res_naive_test   = close_test_naive.values - y_pred_naive_test
res_hw_test      = test_hw.values - y_pred_hw_test.values
res_prophet_test = test_hw.values - y_pred_prophet_test.values

plt.figure(figsize=(12, 3))
plt.plot(dates_test_naive, res_naive_test)
plt.axhline(0, color='k', linestyle='--')
plt.title("Naive (Δlog→price) – остатки на тесте")
plt.xlabel("Дата")
plt.ylabel("Ошибка (y - ŷ)")
plt.grid(True)
plt.tight_layout()
plt.savefig(os.path.join(RESULTS_DIR, "step5_naive_residuals_test.png"))
plt.close()

plt.figure(figsize=(12, 3))
plt.plot(dates_test_hw, res_hw_test)
plt.axhline(0, color='k', linestyle='--')
plt.title("Holt-Winters – остатки на тесте")
plt.xlabel("Дата")
plt.ylabel("Ошибка (y - ŷ)")
plt.grid(True)
plt.tight_layout()
plt.savefig(os.path.join(RESULTS_DIR, "step5_hw_residuals_test.png"))
plt.close()

plt.figure(figsize=(12, 3))
plt.plot(dates_test_hw, res_prophet_test)
plt.axhline(0, color='k', linestyle='--')
plt.title("Prophet – остатки на тесте")
plt.xlabel("Дата")
plt.ylabel("Ошибка (y - ŷ)")
plt.grid(True)
plt.tight_layout()
plt.savefig(os.path.join(RESULTS_DIR, "step5_prophet_residuals_test.png"))
plt.close()

save_residuals_acf(res_naive_test,   "ACF остатков: Naive (Δlog→price) – test", "acf_step5_naive_residuals_test.png", lags=60)
save_residuals_acf(res_hw_test,      "ACF остатков: Holt-Winters – test",       "acf_step5_hw_residuals_test.png",    lags=60)
save_residuals_acf(res_prophet_test, "ACF остатков: Prophet – test",            "acf_step5_prophet_residuals_test.png", lags=60)

start_test = dates_test_hw[0]
end_zoom = start_test + pd.DateOffset(months=2)

mask_hw = (dates_test_hw >= start_test) & (dates_test_hw < end_zoom)

dates_zoom = dates_test_hw[mask_hw]
real_zoom = test_hw.values[mask_hw]
hw_zoom = y_pred_hw_test.values[mask_hw]

mask_ts = (dates_test_ts >= start_test) & (dates_test_ts < end_zoom)
dates_ts_zoom = dates_test_ts[mask_ts]
gd_zoom = y_pred_test_reg[mask_ts]

mask_naive = (dates_test_naive >= start_test) & (dates_test_naive < end_zoom)
dates_naive_zoom = dates_test_naive[mask_naive]
naive_zoom = y_pred_naive_test[mask_naive]

if getattr(start_test, "tz", None) is not None:
    start_zoom_naive = start_test.tz_convert(None)
    end_zoom_naive   = end_zoom.tz_convert(None)
else:
    start_zoom_naive = start_test
    end_zoom_naive   = end_zoom

prophet_zoom = y_pred_prophet_test[
    (y_pred_prophet_test.index >= start_zoom_naive) &
    (y_pred_prophet_test.index < end_zoom_naive)
]

plt.figure(figsize=(12, 4))
plt.plot(dates_zoom, real_zoom, label='Real (test)', linewidth=2)
plt.plot(dates_zoom, hw_zoom, label='Holt-Winters', alpha=0.7)

plt.plot(prophet_zoom.index, prophet_zoom.values, label='Prophet', alpha=0.7)

plt.plot(dates_ts_zoom, gd_zoom, label='GD regression (Δlog→price)', alpha=0.9, linestyle='--')
plt.plot(dates_naive_zoom, naive_zoom, label='Naive (Δlog→price)', alpha=0.9, linestyle='-.')

plt.title("Первые 2 месяца тестовой части: сравнение моделей")
plt.xlabel("Дата")
plt.ylabel("Цена, USD")
plt.legend()
plt.grid(True)
plt.tight_layout()
plt.savefig(os.path.join(RESULTS_DIR, "step5_zoom_first_2_months.png"))
plt.close()

metrics_all = {
    "GD (Δlog→price)": regression_metrics(y_test_price, y_pred_test_reg, return_all=True),
    "GD (Close)":      regression_metrics(y_test_price, y_pred_test_reg_price, return_all=True),
    "Naive (Δlog→price)": regression_metrics(close_test_naive.values, y_pred_naive_test, return_all=True),
    "Holt-Winters":    regression_metrics(test_hw.values, y_pred_hw_test.values, return_all=True),
    "Prophet":         regression_metrics(test_hw.values, y_pred_prophet_test.values, return_all=True),
}
print_all_model_metrics(metrics_all)
