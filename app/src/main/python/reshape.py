import numpy as np
import java

def utils_gen_windows(data_length, window_size, step_width):
    "Generate indices for window-slizes with given length & step width."
    start = 0
    while start < data_length:
        yield start, start + window_size
        start += step_width

def reshape(raw_data, window_size, step_width):
    '''
    input: 
        [n_input,9]
            n_input = n_sec * frequence
            9, data from 3 sensors
    
    output: 
        [n_output, window_size, 9]
            n_output = 
    '''
    python_raw_data = java.cast(java.jarray(java.jarray(java.jfloat)), raw_data)
    raw_data = np.array(python_raw_data)
    new_feat_count = ((raw_data.shape[0] - window_size) // step_width) + 1
    reshaped_feat = np.empty((new_feat_count, window_size, 9))
    for idx, window in enumerate(
        utils_gen_windows(raw_data.shape[0], window_size, step_width)
    ):
        new_row = raw_data[window[0] : window[1]]
        if idx < new_feat_count:
            reshaped_feat[idx, :] = new_row

    return reshaped_feat