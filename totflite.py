import tensorflow as tf

# Load the model
model = tf.keras.models.model_from_json(open('./Models/user_c/trainedModel.json').read())
model.load_weights('./Models/user_c/weights.h5')

# Convert the model
converter = tf.lite.TFLiteConverter.from_keras_model(model)
# converter._experimental_lower_tensor_list_ops = False
# converter.target_spec.supported_ops = [tf.lite.OpsSet.TFLITE_BUILTINS, tf.lite.OpsSet.SELECT_TF_OPS]
tflite_model = converter.convert()

# Save the model 
with open('./Models/user_c/model.tflite', 'wb') as f:
    f.write(tflite_model)
