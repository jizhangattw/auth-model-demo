from sklearn.preprocessing import MinMaxScaler
import numpy as np
import java

def scale_data(raw_data):
    '''
    :param raw_data: 2D array if before reshape [n,9]  if after gen feature [n, 64]
    :return: scaled data by column
    '''
    scaler = MinMaxScaler()
    python_raw_data = java.cast(java.jarray(java.jarray(java.jfloat)), raw_data)
    X = scaler.fit_transform(np.array(python_raw_data))
    return X