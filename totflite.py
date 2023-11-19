import tensorflow as tf

# Load the model
model = tf.keras.models.model_from_json(open('./model/trainedModel.json').read())
model.load_weights('./model/weights.h5')

# Convert the model
converter = tf.lite.TFLiteConverter.from_keras_model(model)
converter._experimental_lower_tensor_list_ops = False
converter.target_spec.supported_ops = [tf.lite.OpsSet.TFLITE_BUILTINS, tf.lite.OpsSet.SELECT_TF_OPS]
tflite_model = converter.convert()

# Save the model
with open('./model/model.tflite', 'wb') as f:
    f.write(tflite_model)
