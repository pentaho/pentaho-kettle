# Run webSpoon on Kubernetes

### These are the old instruction to be used if you are deploying from:
### https://github.com/HiromuHota/pentaho-kettle/tree/webspoon-9.0/docker/k8s

Clone this repository and run this command to create required resources (`Deployment` and `Service`).

```sh
$ kubectl create -f ./k8s
deployment.apps/webspoon created
service/webspoon created
```

Check that webspoon is running.

```sh
$ kubectl get pod
NAME                        READY   STATUS              RESTARTS   AGE
webspoon-78767c7f57-b9fzd   1/1     Running             0          5m7s
```

Do port forwarding of `8080` port of the service to local machine.

```sh
kubectl port-forward service/webspoon 8080:8080 &
```

While port forwarding, access to the port of local machine.

```
http://localhost:8080
```

# Shared `~/.kettle` and `~/.pentaho` directory

Pods share `~/.kettle` and `~/.pentaho` directory as PersistentVolumeClaim (PVC).
If you want to deploy these PVCs to a Kubernetes cluster which only supports `ReadWriteOnce`, you can configure YAML files.

Edit `kettle-pvc.yaml` and `pentaho-pvc.yaml`.

```yaml
  # - ReadWriteMany # Comment out
  - ReadWriteOnce # Uncomment
```

# Customize

## Add `web.xml` etc.

Create your own `web.xml` file and run this command to make ConfigMap including the `web.xml`.

```sh
$ kubectl create configmap webspoon-config-cm --from-file web.xml
```

Then, edit `deployment.yaml` and uncomment this section to mount ConfigMap to containers.

```yaml
        volumeMounts:
        ...
        # - mountPath: /usr/local/tomcat/webapps/spoon/WEB-INF/web.xml
        #   name: webspoon-config-cm
        #   subPath: web.xml
        ...
      volumes:
      # - name: webspoon-config-cm
      #   configMap:
      #     name: webspoon-config-cm
```

File locations by default

| File | Mount target in webSpoon pod |
|-|-|
| `web.xml` | `/usr/local/tomcat/webapps/spoon/WEB-INF/web.xml` |
| `catalina.policy` | `/usr/local/tomcat/conf/catalina.policy` |
| `security.xml` | `/usr/local/tomcat/webapps/spoon/WEB-INF/spring/security.xml`|

# Tear down

```sh
$ kubectl delete -f ./k8s
# If you created ConfigMap
$ kubectl delete configmap webspoon-config-cm
```