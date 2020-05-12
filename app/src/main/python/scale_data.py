from sklearn.preprocessing import MinMaxScaler
import numpy as np
import java
from os.path import dirname, join


def _scale_data(X, data_min_=None, data_max_=None, save_scaler=False):
    scaler = MinMaxScaler()
    if data_min_ is not None:
        scaler.fit_transform(np.array([data_min_, data_max_]))
        scaled_data = scaler.transform(X)
    else:
        scaled_data = scaler.fit_transform(X)
        if save_scaler:
            np.save(join(dirname(__file__),"scaler.npy"), np.vstack((scaler.data_min_, scaler.data_max_)))

    if np.all(scaled_data >= -0.4) and np.all(scaled_data <= 1.4):
        return scaled_data
    else:
        np.save(join(dirname(__file__),"null.npy"),scaled_data)
        return None


def scale_data(raw_data, load_scaler=False, save_scaler=False):
    '''
    :param raw_data: 2D array if before reshape [n,9]  if after gen feature [n, 64]
    :return: scaled data by column or None
    '''
    python_raw_data = java.cast(java.jarray(java.jarray(java.jfloat)), raw_data)
    X = np.array(python_raw_data)
    if load_scaler:
        scaler_min_max_array = np.load(join(dirname(__file__),"scaler.npy"))
        return _scale_data(X, data_min_=scaler_min_max_array[0], data_max_=scaler_min_max_array[1])
    else:
        return _scale_data(X, save_scaler=save_scaler)