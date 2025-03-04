# Launch the Application by Kubernetes

## Intro
-  Instead of launching the app with `docker-compose`, we can also deploy it using Kubernetes. This folder provides two types of Kubernetes configurations:
   - Auto-scaling
     - This configuration enables automatic scaling of `simple-api` by defining the `spec.replicas` property in `deployment.yaml`. Kubernetes will manage the scaling dynamically based on resource usage. 
   - Manual-scaling
     - This configuration replicates the behavior of the original `docker-compose` setup, where scaling is done manually by specifying the number of replicas in `deployment.yaml`.

## Prerequisites (For macOS only)
### Install minikube and kubectl
- Install minikube:
```
$ brew install minikube
```
- Install kubectl:
``` 
$ brew install kubectl
```
- Start minikube:
```
$ minikube start
```
- Check Kubernetes status:
```
$ kubectl cluster-info
$ kubectl get nodes
```
### Prepare the docker images
- Using the following commands for preparing your images and pushing them to the Docker Hub:
```
$ docker login

$ docker tag routing_api:latest YOUR_DOCKER_HUB_USERNAME/routing_api:latest
$ docker tag simple_api:latest YOUR_DOCKER_HUB_USERNAME/simple_api:latest

$ docker push YOUR_DOCKER_HUB_USERNAME/routing_api:latest
$ docker push YOUR_DOCKER_HUB_USERNAME/simple_api:latest
```
- For example, here are the example images:
  - [routing_api](https://hub.docker.com/repository/docker/nekowandrer/routing_api/general)
  - [simple_api](https://hub.docker.com/repository/docker/nekowandrer/simple_api/general)

## Auto Scaling Deployment
- Make sure you're right under the root directory of the project (round_robin_api)
- Change the directory according to the configuration you want to use:
  - For auto-scaling, go to: `kubernetes/auto-scaling`
  - For manual-scaling, go to: `kubernetes/manual-scaling`
- Execute the following command for launching the Kubernetes cluster:
```
$ kubectl apply -f service.yaml -f deployment.yaml
```
- You should see the following messages in the terminal window:
```
service/simple-api-service created
service/routing-api-service created
deployment.apps/simple-api-deployment created
deployment.apps/routing-api-deployment created
```
- Check the status of all pods:
```
$ kubectl get pods
```
- Make sure you can see all the pods are ready and running like the following:
```commandline
NAME                                     READY   STATUS    RESTARTS   AGE
routing-api-deployment-d694cdbcf-vcw78   1/1     Running   0          78s
simple-api-deployment-7fbcfb6c56-2cpkp   1/1     Running   0          78s
simple-api-deployment-7fbcfb6c56-ks4vs   1/1     Running   0          78s
simple-api-deployment-7fbcfb6c56-m2hg5   1/1     Running   0          78s
simple-api-deployment-7fbcfb6c56-mq9p9   1/1     Running   0          78s
simple-api-deployment-7fbcfb6c56-wslzc   1/1     Running   0          78s
```
- Then, check the status of all services:
```
$ kubectl get services
```
- You should see something like the following in the terminal window:
```
NAME                  TYPE        CLUSTER-IP    EXTERNAL-IP   PORT(S)          AGE
kubernetes            ClusterIP   10.96.0.1     <none>        443/TCP          3h50m
routing-api-service   NodePort    10.96.238.8   <none>        8080:32090/TCP   20s
simple-api-service    ClusterIP   10.100.8.87   <none>        8080/TCP         20s
```
- For simplicity, use the following port forward command for exposing the service to the local machine:
```
$ kubectl port-forward svc/routing-api-service 8080:8080
```
- Then you can access the app at http://localhost:8080
  - <img src="https://github.com/user-attachments/assets/59ca1562-44c8-4085-8dcd-9651c4e3d5a4" width=1000 alt="">
- After testing, use the following command to stop the Kubernetes cluster:
```
$ kubectl delete -f service.yaml -f deployment.yaml --wait
```
- Make sure all pods are deleted:
```
$ kubectl get pods
```
- Make sure all services are deleted except for the `kubernetes` service:
```
$ kubectl get services
```
- Deployment with Kubernetes is primarily used to demonstrate cloud-native concepts. For local development and testing, using docker-compose is still the recommended deployment method.
- Additionally, the ports in the manual-scaling setup can also be extracted through `kustomization.yaml`, but for simplicity, this step has been omitted.
