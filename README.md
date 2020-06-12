# Template Service Broker

> Service Broker for HyperCloud Service created based on template

**Architecture**

![Architecture](https://user-images.githubusercontent.com/65938055/84469785-d4120000-acbc-11ea-8524-51bc2a9812fc.png)



## Install Template Service Broker

- [Namespace](#namespace)
- [ServiceAccount](#serviceaccount)
- [Role](#role)
- [RoleBinding](#rolebinding)
- [Deployment](#deployment)
- [Service](#service)
- [Test](#test)
- [ServiceBroker](#servicebroker)

---

#### Namespace
> Create your own namespace.

```shell
$ kubectl create namespace example-ns
```

---

#### ServiceAccount
> Create a service account to pass permission to template service broker server.

```shell
$ kubectl create serviceaccount example-account -n example-ns
```

---

#### Role
> Create a role. Need Permission : `tmax.io` & `servicecatalog.k8s.io`

```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: service-broker-role
  namespace: example-ns
rules:
- apiGroups: ["tmax.io"]
  resources: ["*"]
  verbs: ["*"]
- apiGroups: ["servicecatalog.k8s.io"]
  resources: ["*"]
  verbs: ["*"]
```

---

#### RoleBinding
> Create a RoleBinding. Bind Role and Service Account.

```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: service-broker-rolebinding
  namespace: example-ns
subjects:
- kind: ServiceAccount
  name: example-account
roleRef:
  kind: Role
  name: service-broker-role
  apiGroup: rbac.authorization.k8s.io
```

---

#### Deployment
> Create Template Service Broker Server. Mount the service account.

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: template-service-broker
  namespace: example-ns
  labels:
    hypercloud4: template-service-broker
    name: template-service-broker
spec:
  replicas: 1
  selector:
    matchLabels:
      hypercloud4: template-service-broker
  template:
    metadata:
      name: template-service-broker
      labels:
        hypercloud4: template-service-broker
    spec:
      containers:
      - name: template-service-broker
        image: tmaxcloudck/template-service-broker:b4.0.0.4
        imagePullPolicy: Always
        env:
        - name: BackIp
          valueFrom:
            fieldRef:
              fieldPath: status.podIP
        - name: INSTANCEUUID
          valueFrom:
            fieldRef:
              fieldPath: metadata.name
        - name: TZ
          value: Asia/Seoul
        ports:
        - containerPort: 28677
        resources:
          limits:
            cpu: "0.5"
            memory: "1Gi"
          requests:
            cpu: "0.5"
            memory: "1Gi"
      serviceAccountName: example-account
```

---

#### Service
> Create Service to access the server.

```yaml
apiVersion: v1
kind: Service
metadata:
  name: template-service-broker-service
  namespace: example-ns
spec:
  ports:
  - name: "port1"
    port: 28677
    protocol: TCP
    targetPort: 28677
  selector:
    hypercloud4: template-service-broker
  type: LoadBalancer
```

---

#### Test
> Call the catalog service for test.

```shell
$ curl -X GET http://{SERVER_IP}:28677/v2/catalog
```

---

#### ServiceBroker
> Create Service Broker in your own namespace.

```yaml
apiVersion: servicecatalog.k8s.io/v1beta1
kind: ServiceBroker
metadata:
  name: template-service-broker
  namespace: example-ns
spec:
  url: http://{SERVER_IP}:28677
```
