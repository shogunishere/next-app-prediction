# Predicting next app

To attempt to make the time of searching for the correct app shorter, we wish to create an application that predicts the next app that the user will open. We will explore various datapoints that could contribute to better prediction, train a machine learning model and use it to build an Android application predicting the next app. If we have time we will make use of sensor data as it could add extra contextual information.

## Get started

`git clone https://github.com/shogunishere/next-app-prediction`

`cd next-app-prediction`

unzip lsapp.tsv.gz and put lsapp.tsv into root level of project

## Create conda environment

`conda create --name next_app_prediction python=3.10`

`conda activate next_app_prediction`

optional(open with VsCode): `code .`

## Acknowledgments

Dataset used from this repository: https://github.com/aliannejadi/LSApp
