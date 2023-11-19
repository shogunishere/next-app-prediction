import numpy as np
import tensorflow as tf
from sklearn.preprocessing import MinMaxScaler, LabelEncoder
import pandas as pd
import numpy as np
import keras

# Data preprocessing functions
#removing the redundant data.
def clean_data(data):
    data=data[~(data == 'Screen off (locked)').any(axis=1)]
    data=data[~(data == 'Screen on (unlocked)').any(axis=1)]
    data=data[~(data == 'Screen off (unlocked)').any(axis=1)]
    data=data[~(data == 'Screen on (locked)').any(axis=1)]
    data=data[~(data == 'Screen on').any(axis=1)]
    data=data[~(data == 'Screen off').any(axis=1)]
    data=data[~(data == 'Device shutdown').any(axis=1)]
    data=data[~(data == 'Device boot').any(axis=1)]
    data=data.dropna()
    data.index=range(len(data))
    return data

def encode_data(data):
    label_encoder_app=LabelEncoder()
    encoded_data=label_encoder_app.fit_transform(data.iloc[:,0:1])
    encoded_data=pd.DataFrame(data=encoded_data)
    return [encoded_data, label_encoder_app]

#splitting the training and testing data.
def split_into_train_test_set(encoded_data):
    train_set=encoded_data.iloc[:1901,0:1].values
    test_set=encoded_data.iloc[1901:,0:1].values
    return [train_set, test_set]


# Load the TFLite model using TensorFlow
interpreter = tf.lite.Interpreter(model_path='./model/model.tflite')
interpreter.allocate_tensors()

# Get input and output details
input_details = interpreter.get_input_details()
print("input_details: ")
print(input_details)

output_details = interpreter.get_output_details()
print("output_details: ")
print(output_details)

# Prepare test data (modify as per your model's requirement)
#test_data = np.array(np.random.random_sample(input_details[0]['shape']), dtype=np.float32)
data=pd.read_csv('./dataset.csv')
data = clean_data(data)
[encoded_data, label_encoder_app] = encode_data(data)
[train_set, test_set] = split_into_train_test_set(encoded_data)

scaler=MinMaxScaler(feature_range=(0,1))
training_set_scaled=scaler.fit_transform(train_set)

# Transoform data to easily feed into the model
X_train=[]
y_train=[]

for i in range(10,1901):
    X_train.append(training_set_scaled[i-10:i,0])
    y_train.append(train_set[i,0])

X_train=np.array(X_train)
X_train = np.reshape(X_train, (X_train.shape[0], X_train.shape[1], 1))

label_encoder_y=LabelEncoder()
y_train=label_encoder_y.fit_transform(y_train)
y_train=np.array(y_train)
y_train= keras.utils.to_categorical(y_train, num_classes=36)

#testing
total_dataset=encoded_data.iloc[:,0:1]
inputs=total_dataset[len(total_dataset)-len(test_set)-10:].values
inputs=inputs.reshape(-1,1)
inputs=scaler.transform(inputs)
decoded_input=scaler.inverse_transform(inputs)
X_test=[]
for i in range(10,397):
    X_test.append(inputs[i-10:i, 0])
X_test=np.array(X_test)
X_test = np.reshape(X_test, (X_test.shape[0], X_test.shape[1], 1))

X_test = X_test.astype(np.float32)

print("X_test: ")
print(X_test)

print("X_test.shape")
print(X_test.shape)

# Run the model
for i in range(X_test.shape[0]): #
    test_input = X_test[i:i+1]  # Reshape to maintain the 3D input structure
    
    #print("test_input: ")
    #print(test_input)

    interpreter.set_tensor(input_details[0]['index'], test_input)
    interpreter.invoke()
    output_data = interpreter.get_tensor(output_details[0]['index'])
    
    #print("output_data: ")
    #print(output_data)