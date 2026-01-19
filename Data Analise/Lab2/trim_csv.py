import pandas as pd
import numpy as np

df = pd.read_csv('data.csv')

columns_to_drop = ["class", "tank_id", "isPrem_True", "isPrem_False", "wn8", "wnx"]
existing_columns_to_drop = [col for col in columns_to_drop if col in df.columns]

df = df.drop(columns=existing_columns_to_drop)

missing_columns = df.columns[df.isnull().any()].tolist()

if missing_columns:
    for col in df.columns:
        if df[col].isna().any():
            if pd.api.types.is_numeric_dtype(df[col]):
                med = df[col].median()
                df[col] = df[col].fillna(med)
else:

df.to_csv('data_cleaned.csv', index=False)
