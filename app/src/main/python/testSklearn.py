import pickle
from os.path import dirname, join
import numpy as np
from sklearn.svm import OneClassSVM

def testOcsvm():
    train_X = np.load(join(dirname(__file__), 'train_X.npy'))
    print("train input data shape : ")
    print(train_X.shape)
    print(type(train_X[0,:].tolist()[0]))
    train_and_save_model(train_X)

    print("test input data shape : ")
    print(train_X[0:1].shape)
    result = predict(train_X[0:1])
    print("output data shape : ")
    print(result.shape)
    print("result : ")
    print(result)
    print(type(result.tolist()[0]))

def train_and_save_model(train_X, nu=0.092, gamma=1.151, model_file_name='ocsvm.pickle'):
    '''
    :param train_X: (n, 64)  float
    :param nu:
    :param gamma:
    :param model_file_name:
    :return:
    '''
    model = OneClassSVM(kernel="rbf", nu=nu, gamma=gamma)
    model.fit(train_X)
    with open(join(dirname(__file__), model_file_name), 'wb') as f:
        pickle.dump(model, f)
    print("save model done!")

def predict(test_X, model_file_name='ocsvm.pickle'):
    '''
    :param test_X: (n, 64)  float
    :param model_file_name:
    :return: (n,)  int
    '''
    with open(join(dirname(__file__), model_file_name),'rb') as f:
        model = pickle.load(f)
    return model.predict(test_X)