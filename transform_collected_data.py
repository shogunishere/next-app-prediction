import pandas as pd
from datetime import datetime

def transform_csv2(file_path):
    # Read the CSV file
    df_csv2 = pd.read_csv(file_path, header=None, names=['Timestamp', 'App Name'])

    # Convert Timestamp to datetime
    df_csv2['Timestamp'] = pd.to_datetime(df_csv2['Timestamp'])

    # Sort by timestamp
    df_csv2 = df_csv2.sort_values(by='Timestamp')

    # Calculate duration
    df_csv2['Duration'] = df_csv2['Timestamp'].diff().shift(-1)

    # Format date as mm/dd/yy
    df_csv2['Date'] = df_csv2['Timestamp'].dt.strftime('%m/%d/%y')

    # Format time as hh:mm:ss (without microseconds) and keep it as datetime.time for comparison
    df_csv2['Time'] = df_csv2['Timestamp'].dt.time

    # Format duration, removing "0 days" and handling the last record duration
    df_csv2['Duration'] = df_csv2['Duration'].dt.total_seconds().fillna(0).astype(int)
    df_csv2['Duration'] = df_csv2['Duration'].apply(lambda x: str(pd.to_timedelta(x, unit='s')).split(' ')[-1])

    # Rearrange and rename columns to match CSV1 format
    df_transformed = df_csv2[['App Name', 'Date', 'Time', 'Duration']]

    # Renaming columns to match CSV1
    df_transformed.columns = ['App name', 'Date', 'Time', 'Duration']

    # Filter out entries between 3 AM and 5 AM
    df_filtered = df_transformed[~df_transformed['Time'].between(datetime.strptime('03:00:00', '%H:%M:%S').time(),
                                                                datetime.strptime('05:00:00', '%H:%M:%S').time())]

    return df_filtered

# Specify your file path here
file_path = './Datasets/clara_data.csv'
transformed_data = transform_csv2(file_path)
print(transformed_data.head())

unique_apps = transformed_data['App name'].unique()

print("Unique Apps:")
print(unique_apps)

unique_app_count = len(unique_apps)
print("Total Number of Unique Apps:", unique_app_count)

# Optionally, write the transformed data to a new CSV file
transformed_data.to_csv('./Datasets/clara_data_transformed.csv', index=False)
