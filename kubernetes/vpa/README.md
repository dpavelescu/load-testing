# VPA (Vertical Pod Autoscaler) Setup

## Current Setup (Using Helm)

The VPA is now installed using Helm for better dependency management and compatibility.

### Installation Commands

```bash
# Install VPA using Helm with custom resource limits
helm repo add fairwinds-stable https://charts.fairwinds.com/stable
helm install vpa fairwinds-stable/vpa --namespace vpa-system --create-namespace \
  --set recommender.resources.requests.memory=200Mi \
  --set recommender.resources.limits.memory=300Mi \
  --set updater.resources.requests.memory=150Mi \
  --set updater.resources.limits.memory=200Mi \
  --set admissionController.resources.requests.memory=100Mi \
  --set admissionController.resources.limits.memory=150Mi
```

### Verify Installation

```bash
kubectl get pods -n vpa-system
kubectl get vpa -A
```

### Current VPA Configuration

- **API Version**: v1beta2 (provides better status information than v1)
- **Update Mode**: Off (recommendation only, no automatic updates)
- **Resource Policy**: Configured with min/max bounds
- **Target Deployment**: resource-sizing-service in load-testing namespace

### Files in this Directory

- `04-resource-sizing-vpa.yaml` - VPA configuration for our Spring Boot service
- `vpa-values.yaml` - Helm values used for VPA installation
- `README.md` - This file

### Note

Previous manual installation files (CRDs, RBAC, components) have been removed 
as they are now managed by Helm installation.
