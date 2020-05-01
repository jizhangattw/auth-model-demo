import pickle
from os.path import dirname, join
import numpy as np
from sklearn.svm import OneClassSVM

def testOcsvm():
    train_X = np.load(join(dirname(__file__), 'train_X.npy'))
    model = OneClassSVM(kernel="rbf", nu=0.092, gamma=1.151)
    model.fit(train_X)
    with open(join(dirname(__file__), 'ocsvm_new.pickle'), 'wb') as f:
            pickle.dump(model, f)
    print("save model!")
    with open(join(dirname(__file__), 'ocsvm_new.pickle'),'rb') as f:
            clf2 = pickle.load(f)
    print(clf2.predict(train_X[0:1]))
