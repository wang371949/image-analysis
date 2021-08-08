# machine-learning
Image labeling： the project completed during Internship with Australian National library in 2020 

Command to start: 
mvn clean install
mvn spring-boot:run 

Parameters for cloud labeling services
service = GL : Google Cloud Vision Image Labeling
serivce = AL : AWSReKognition Image Labeling
service = ML : Microsoft Azure Image Labeling
service = MD : Microsoft Azure Image Description

example url request format: 
http://localhost:8080/label/nla.obj-131286956?service=GL&&service=AL&&service=ML&&service=MD
