# -*- coding: utf-8 -*-
"""
Created on Fri Jan  4 20:37:35 2019

@author: Suvab Baral, Brin Colnar
"""

import sys
import math
import pandas as pd
import numpy as np
import keras
from keras.models import Sequential
from keras.layers import Dense, Flatten
from keras.layers import LSTM
from keras.layers import Dropout
from keras.layers import BatchNormalization
from sklearn.preprocessing import MinMaxScaler, LabelEncoder, OneHotEncoder
from getModel import getTrainedModel
from saveModel import saveRNNModel

total_size = -1  
train_size = -1 
test_size =  -1  
unique_apps = -1 

#removing the redundant data.
def clean_data(data):

    # First remove specific unwanted apps
    unwanted_apps = [
        'com.example.nextapp', 'com.android.launcher', 'com.huawei.android.launcher', 'com.sec.android.app.launcher',
        'Screen off (locked)', 'Screen off (unlocked)', 'Screen on (unlocked)', 
        'Screen on (locked)', 'Screen on', 'Screen off', 'Device shutdown', 'Device boot'
    ]
    for app in unwanted_apps:
        data = data[data['App name'] != app] 

    # Drop rows with any NaN values
    data=data.dropna()

    # Keep 20 most used apps only
    top_20_apps = data['App name'].value_counts().head(20).index.tolist()

    print("top_20_apps")
    print(top_20_apps)

    # Keep only rows with these top 20 apps
    data = data[data['App name'].isin(top_20_apps)]

    data.index=range(len(data))

    return data

def encode_data(data):
    label_encoder_app=LabelEncoder()
    encoded_data=label_encoder_app.fit_transform(data.iloc[:,0:1])
    encoded_data=pd.DataFrame(data=encoded_data)
    return [encoded_data, label_encoder_app]

def one_hot_encode_data(data):
    one_hot_encoder = OneHotEncoder(sparse=False)  # Set sparse=False to return a regular array
    # Assuming the first column of the dataframe needs to be one-hot encoded
    encoded_data = one_hot_encoder.fit_transform(data.iloc[:, 0:1])
    encoded_data = pd.DataFrame(data=encoded_data, columns=one_hot_encoder.get_feature_names_out())
    return [encoded_data, one_hot_encoder]

#splitting the training and testing data.
def split_into_train_test_set(encoded_data, use_one_hot_encoded):
    if use_one_hot_encoded:
        train_set=encoded_data.iloc[:train_size].values
        test_set=encoded_data.iloc[(train_size+validation_size):].values
    else:
        train_set=encoded_data.iloc[:train_size,0:1].values
        test_set=encoded_data.iloc[(train_size+validation_size):,0:1].values
    return [train_set, test_set]

def getRNNModel(X_train, use_preTrained_model, use_one_hot_encoded):
    if (use_preTrained_model == 'True'):
        print('***************Using pretrained model***************')
        RNNModel = getTrainedModel()
    else:
        print('***************Creating model and start training***************')    

        if use_one_hot_encoded:
            model = Sequential()

            # Flatten the input to make it suitable for a Dense layer
            model.add(Flatten(input_shape=X_train.shape[1:]))

            # First Dense layer
            model.add(Dense(256, activation='relu'))
            model.add(Dropout(0.3))

            # Second Dense layer
            model.add(Dense(128, activation='relu'))
            model.add(Dropout(0.2))

            # Output layer
            output_units = y_train.shape[1] if use_one_hot_encoded else 1
            activation_function = 'softmax' if use_one_hot_encoded else 'sigmoid'
            model.add(Dense(output_units, activation=activation_function))

            # Compile the model
            model.compile(optimizer='adam', loss='categorical_crossentropy', metrics=['accuracy'])
            return model
        else:
            RNNModel = Sequential()

            RNNModel.add(LSTM(units = 200,return_sequences = True, input_shape=(X_train.shape[1], 1)))
            RNNModel.add(Dropout(rate=0.3))

            RNNModel.add(LSTM(units =200, return_sequences = True))
            RNNModel.add(Dropout(rate=0.3))
            
            RNNModel.add(LSTM(units =200, return_sequences = True))
            RNNModel.add(Dropout(rate=0.3))

            RNNModel.add(LSTM(units = 200, return_sequences = True))
            RNNModel.add(Dropout(rate=0.3))

            RNNModel.add(LSTM(units =200, return_sequences = False)) #False caused the exception ndim
            RNNModel.add(Dropout(rate=0.3))

            RNNModel.add(Dense(units= unique_apps,activation='sigmoid'))

            RNNModel.compile(optimizer = 'rmsprop', loss = 'categorical_crossentropy', metrics = ['accuracy'])
            return RNNModel

def getMyRNNModel(X_train):
    model = keras.Sequential()
    model.add(LSTM(64,input_shape=(X_train.shape[1],1)))
    model.add(BatchNormalization())
    model.add(Dense(unique_apps))
    print(model.summary())
    model.compile(
        loss='categorical_crossentropy',
        optimizer="rmsprop",
        metrics=["accuracy"],
    )
    return model

use_preTrained_model =  False 
if (len(sys.argv) > 1):
    use_preTrained_model = sys.argv[1]

filename = './Datasets/dataset.csv'
data=pd.read_csv(filename)

data = clean_data(data)

use_validate = False

total_size = len(data.index)
print(f"total_size: {total_size}")
train_size = math.floor(total_size * 0.8)
print(f"train_size: {train_size}")

validation_size = 0
if use_validate:
    validation_size = math.floor(total_size * 0.1)
    print(f"validation_size: {validation_size}")

    test_size = math.floor(total_size * 0.1)
    print(f"test_size: {test_size}")
else:
    test_size = math.floor(total_size * 0.2)
    print(f"test_size: {test_size}")

unique_apps = len(data['App name'].unique())
print(f"num. of unique_apps: {unique_apps}")
print(f"unique_apps: {data['App name'].unique()}")

sequence_length = 10


use_one_hot_encoded = True

if use_one_hot_encoded:
    [encoded_data, encoder] = one_hot_encode_data(data)
else:
    [encoded_data, encoder] = encode_data(data)

# Print mapping
if use_one_hot_encoded:
    # Assuming the first column of 'data' contains the app names
    unique_apps = data.iloc[:, 0].unique()
    print("One-hot Encoding Mapping:")

    # Iterating over each unique app to print its one-hot encoded vector
    for app in unique_apps:
        # Reshape 'app' for one-hot encoding
        app_vector = np.array([app]).reshape(-1, 1)
        encoded_vector = encoder.transform(app_vector)

        # Printing the mapping
        print(f"{app}: {encoded_vector}")
else:
    print("Label Encoding Mapping: ")
    app_indices = np.array([i for i in range(unique_apps)])


    print(app_indices)
    print(encoder.inverse_transform(app_indices))

[train_set, test_set] = split_into_train_test_set(encoded_data, use_one_hot_encoded)

print("train_set[0]")
print(train_set[0])

print("len(train_set)")
print(len(train_set))

print("len(test_set)")
print(len(test_set))

if not use_one_hot_encoded:
    scaler=MinMaxScaler(feature_range=(0,1))
    training_set_scaled=scaler.fit_transform(train_set)

# Transform data to easily feed into the model
X_train=[]
y_train=[]

if use_one_hot_encoded:
    for i in range(sequence_length,len(train_set)):
        X_train.append(train_set[i-sequence_length:i])
        y_train.append(train_set[i])
else:
    for i in range(sequence_length,len(train_set)):
        X_train.append(training_set_scaled[i-sequence_length:i,0])
        y_train.append(train_set[i,0])

print("y_train[0]")
print(y_train[0])

X_train=np.array(X_train)
if not use_one_hot_encoded:
    X_train = np.reshape(X_train, (X_train.shape[0], X_train.shape[1], 1))

print("X_train[:10]")
print(X_train[:10])

print("X_train.shape")
print(X_train.shape)


if use_one_hot_encoded:
    y_train=np.array(y_train)
else:
    label_encoder_y=LabelEncoder()
    y_train=label_encoder_y.fit_transform(y_train)
    y_train=np.array(y_train)
    y_train= keras.utils.to_categorical(y_train, num_classes=unique_apps)


print("y_train.shape")
print(y_train.shape)

print("y_train[:10]")
print(y_train[:10])

if use_one_hot_encoded:
    total_dataset = encoded_data
else:    
    total_dataset=encoded_data.iloc[:,0:1]

X_valid = []
y_valid = []

# validation
if use_validate == True:
    validation_inputs = total_dataset[len(total_dataset)-len(test_set)-validation_size-sequence_length:].values 
    validation_inputs=validation_inputs.reshape(-1,1)
    validation_inputs=scaler.transform(validation_inputs)
    validation_decoded_input=scaler.inverse_transform(validation_inputs)


    for i in range(sequence_length,validation_size):
        X_valid.append(validation_inputs[i-sequence_length:i,0])
        y_valid.append(validation_inputs[i,0])

    X_valid=np.array(X_valid)
    X_valid=np.reshape(X_valid, (X_valid.shape[0], X_valid.shape[1], 1))

    print("X_valid.shape")
    print(X_valid.shape)

    y_valid=label_encoder_y.fit_transform(y_valid)
    y_valid=np.array(y_valid)
    y_valid= keras.utils.to_categorical(y_valid, num_classes=unique_apps)

    print("y_valid.shape")
    print(y_valid.shape)

#testing
test_inputs=total_dataset[len(total_dataset)-len(test_set)-sequence_length:].values

print("len(test_inputs)")
print(len(test_inputs))

if not use_one_hot_encoded:
    test_inputs=test_inputs.reshape(-1,1)
    test_inputs=scaler.transform(test_inputs)
    decoded_input=scaler.inverse_transform(test_inputs)
X_test=[]

if use_one_hot_encoded:
    for i in range(sequence_length,len(test_inputs)): 
        X_test.append(test_inputs[i-sequence_length:i])
else:
    for i in range(sequence_length,test_size):
        X_test.append(test_inputs[i-sequence_length:i, 0])

X_test=np.array(X_test)

if not use_one_hot_encoded:
    X_test = np.reshape(X_test, (X_test.shape[0], X_test.shape[1], 1))

print("X_train.shape")
print(X_train.shape)

print("X_test.shape")
print(X_test.shape)

#training
RNNModel = getRNNModel(X_train, use_preTrained_model, use_one_hot_encoded)

if (use_preTrained_model != 'True'):

    if use_validate:
        RNNModel.fit(X_train, y_train, validation_data=(X_valid, y_valid), epochs = 10, batch_size = 16)
    else:
        RNNModel.fit(X_train, y_train, epochs = 20, batch_size = 16)

    # print(RNNModel.history.history['acc'])
# print(RNNModel.summary())

predicted_app=RNNModel.predict(X_test, batch_size=16)

print("predicted_app")
print(predicted_app)

print("len(predicted_app[0])")
print(len(predicted_app[0]))


idx = (-predicted_app).argsort() 

def create_one_hot_encoded_vector(i):
    vector = [0 for j in range(idx.shape[1])] 
    vector[i] = 1
    return vector

print("idx.shape")
print(idx.shape)

print("idx: ")
print(idx)

if use_one_hot_encoded:
    idx_onehot = [[] for x in range(idx.shape[0])]

    for i in range(idx.shape[0]):
        for j in range(idx.shape[1]):
            app_index = idx[i,j]
            one_hot_encoded_vector = create_one_hot_encoded_vector(app_index)
            idx_onehot[i].append(one_hot_encoded_vector)

idx_onehot = np.array(idx_onehot)

print("idx_onehot.shape: ")
print(idx_onehot.shape)

print("len(idx_onehot[0])")
print(len(idx_onehot[0]))

#confusion matrix (just for chekcking the accuracy)
if not use_one_hot_encoded:
    cm=np.zeros(shape=(2,2))
    for i in range(predicted_app.shape[0]):
        print(test_set[i])
        print(idx[i,0])
        if(test_set[i]== idx[i,0] or test_set[i]==idx[i,1] or test_set[i]==idx[i,2] or test_set[i]==idx[i,3] ):
            cm[1,1]+=1
        else:
            cm[1,0]+=1



# Add first prediction
if use_one_hot_encoded:
    #index of the highest values.

    predictions = []
    for i in range(idx_onehot.shape[0]):
        one_hot_vector_2d = idx_onehot[i, 0].reshape(1, -1)
        prediction = encoder.inverse_transform(one_hot_vector_2d)
        # Since the output is an array, extract the first element
        predictions.append(prediction[0][0])

    prediction=pd.DataFrame(data=predictions)
    actual_app_used=encoder.inverse_transform(test_set)
    actual_app_used=pd.DataFrame(data=actual_app_used)

    print("prediction: ")
    print(prediction)

    print("actual_app_used: ")
    print(actual_app_used)
else:
    #index of the highest values.
    idx=pd.DataFrame(idx_onehot)
    prediction=encoder.inverse_transform(idx.iloc[:,0:1])
    prediction=pd.DataFrame(data=prediction)
    actual_app_used=encoder.inverse_transform(test_set)
    actual_app_used=pd.DataFrame(data=actual_app_used)

# print("idx: ", idx)
# print("prediction: ")
# print(prediction)
# print("actual_app_used: ")
# print(actual_app_used)
    
print("prediction.shape")
print(prediction.shape)

# Add other 3 predictions
if use_one_hot_encoded:
    idx3_rows = []

    for i in range(idx_onehot.shape[0]):
        one_hot_vector_2d_1 = idx_onehot[i, 1].reshape(1, -1)
        one_hot_vector_2d_2 = idx_onehot[i, 2].reshape(1, -1)
        one_hot_vector_2d_3 = idx_onehot[i, 3].reshape(1, -1)

        decoded_one_hot_vector_1 = encoder.inverse_transform(one_hot_vector_2d_1)[0][0]
        decoded_one_hot_vector_2 = encoder.inverse_transform(one_hot_vector_2d_2)[0][0]
        decoded_one_hot_vector_3 = encoder.inverse_transform(one_hot_vector_2d_3)[0][0]

        idx3_rows.append([prediction.iloc[i,0], decoded_one_hot_vector_1, decoded_one_hot_vector_2, decoded_one_hot_vector_3])

        # print("idx3 as dataframe: ")
        # print(idx3)
        # print("prediction: ")
        # print(prediction)
        #if we want the prediction of the entire 36 apps. USE ' range (1,36) ' in the for loop

        idx3 = pd.DataFrame(idx3_rows, columns=['App1', 'App2', 'App3', 'App4'])
    
    all_predictions = pd.DataFrame(idx3_rows, columns=['App1', 'App2', 'App3', 'App4'])
    print("all_predictions")
    print(all_predictions)

    print("all_predictions.shape")
    print(all_predictions.shape)
else:
    for i in range(1,4):
        idx3=encoder.inverse_transform(idx.iloc[:,i:i+1])
        idx3=pd.DataFrame(data=idx3)    
        # print("idx3 as dataframe: ")
        # print(idx3)
        prediction=pd.concat([prediction,idx3], ignore_index=True, axis=1) # axis=i
        # print("prediction: ")
        # print(prediction)
    #if we want the prediction of the entire 36 apps. USE ' range (1,36) ' in the for loop


if use_one_hot_encoded:
    final_outcome = pd.concat([all_predictions, actual_app_used], axis = 1)
    final_outcome.columns = ['Prediction1', 'Prediction2', 'Prediction3', 'Prediction4', 'Actual App Used']
    print('***********************************FINAL PREDICTION*********************************')
    pd.set_option('display.max_rows', None)
    print(final_outcome)
else:
    final_outcome = pd.concat([prediction, actual_app_used], axis = 1)
    final_outcome.columns = ['Prediction1', 'Prediction2', 'Prediction3', 'Prediction4', 'Actual App Used']
    print('***********************************FINAL PREDICTION*********************************')
    pd.set_option('display.max_rows', None)
    print(final_outcome)


accuracy_1 = (final_outcome['Prediction1'] == final_outcome['Actual App Used']).mean()
accuracy_2 = final_outcome.apply(lambda row: row['Actual App Used'] in [row['Prediction1'], row['Prediction2']], axis=1).mean()
accuracy_3 = final_outcome.apply(lambda row: row['Actual App Used'] in [row['Prediction1'], row['Prediction2'], row['Prediction3']], axis=1).mean()
accuracy_4 = final_outcome.apply(lambda row: row['Actual App Used'] in [row['Prediction1'], row['Prediction2'], row['Prediction3'], row['Prediction4']], axis=1).mean()

print("Model: ")
print(f"Accuracy_1: {accuracy_1:.2%}")
print(f"Accuracy_2: {accuracy_2:.2%}")
print(f"Accuracy_3: {accuracy_3:.2%}")
print(f"Accuracy_4: {accuracy_4:.2%}")

# print all app percentages
for app in data['App name'].unique():
    count = len(data.loc[data['App name'] == app])
    percentage = '{:.2%}'.format(count / total_size)
    print(f"{app}: {percentage}%")

# baselines:

# most popular app
app_popularity = data['App name'].value_counts()

most_popular_app = app_popularity.idxmax()

print(f"Most popular app is: {most_popular_app}")

accuracy = (most_popular_app == final_outcome['Actual App Used']).mean()

print(f"Most popular app accuracy: {accuracy:.2%}")

# random app
unique_apps = data['App name'].unique()

random_predictions = np.random.choice(unique_apps, size=len(final_outcome))

random_accuracy = (random_predictions == final_outcome['Actual App Used']).mean()

print(f"Random app prediction accuracy: {random_accuracy:.2%}")

# most recently used
data=pd.read_csv(filename)
data = clean_data(data)
test_set=data.iloc[train_size:,0:1].values

# Shift the comparison by -1
correct_predictions = sum(predicted == actual for predicted, actual in zip(test_set[0:-2,0], final_outcome['Actual App Used'][2:]))
# We exclude the first prediction and the last actual value due to the shift
total_predictions = len(test_set[1:,0])
accuracy = (correct_predictions / total_predictions) 
print(f"Accuracy after shifting by -1: {accuracy:.2%}")

if (use_preTrained_model != 'True'):
    #final_model_accuracy = RNNModel.history.history['acc']
    #print('Accuracy of the model: ', round(final_model_accuracy[0] * 100, 2))
    saveRNNModel(RNNModel)







