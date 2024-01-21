# AI part: Next app prediction

To generate and train a new model:

```python src/generateModel.py```

Dataset collected over 6 volunteers are in ```Datasets``` folder.

In ```./src/generateModel``` set ```filename``` to appropriate dataset (tranformed version, except for ```dataset.csv```).

Make sure ```use_one_hot_encoded = True``` is set to True.

Use ```totflite.py``` to transform model to TFLite format.

```transform_collected_data.py``` is used to transform dataset into appropriate format.
