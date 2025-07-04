# Kubernetes Pod Resource Sizing Project

## Overview
This project provides a comprehensive solution for accurately determining optimal CPU and memory allocations for Kubernetes pods based on empirical load testing data. The system addresses the critical challenge of right-sizing pod resources to balance performance, cost efficiency, and system reliability while maintaining elasticity for varying load patterns.

## Architecture
- **Spring Boot 3.5 Test Service** - Simulates employee data with configurable memory consumption
- **K6 Load Testing** - Generates various load patterns (steady, spike, ramp-up/down)
- **Minimal Prometheus** - Essential metrics collection for resource sizing
- **Vertical Pod Autoscaler (VPA)** - Baseline resource recommendations
- **Docker Desktop Kubernetes** - Target deployment environment

## Project Structure
```
├── src/                    # Spring Boot application source code
│   ├── main/
│   │   ├── java/
│   │   └── resources/
│   └── test/
├── k6-scripts/            # Load testing scripts
├── kubernetes/            # Kubernetes manifests
│   ├── app/              # Application deployment
│   ├── monitoring/       # Minimal Prometheus setup
│   └── vpa/              # VPA configurations
├── docs/                 # Project documentation
└── .taskmaster/          # Task Master project management
```

## Getting Started

### Prerequisites
- Docker Desktop with Kubernetes enabled
- Java 21+
- Maven 3.6+
- K6 load testing tool
- kubectl

### Quick Setup
1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd load-testing
   ```

2. **Build the Spring Boot service**
   ```bash
   cd src
   mvn clean package
   ```

3. **Deploy application to Kubernetes**
   ```bash
   kubectl apply -f kubernetes/app/
   ```

4. **Set up monitoring**
   ```bash
   kubectl apply -f kubernetes/monitoring/
   ```
   *For detailed monitoring setup, see [docs/monitoring-setup.md](docs/monitoring-setup.md)*

5. **Run load tests**
   ```bash
   k6 run k6-scripts/basic-load-test.js
   ```

## Key Features
- **Configurable Data Simulation** - Variable employee data with adjustable string field sizes
- **Multiple Load Patterns** - Steady, spike, ramp-up, and ramp-down testing scenarios
- **Resource Isolation** - Monitoring components deployed in separate namespace
- **Automated Analysis** - Resource usage correlation with load patterns
- **HPA Recommendations** - Horizontal scaling calculations and configurations
- **Comprehensive Reporting** - Automated sizing recommendation reports
- **Minimal Monitoring** - Essential Prometheus metrics without complexity

## Security Notes
- Monitoring uses simple, password-free setup for development
- All components run in isolated namespaces
- See [docs/monitoring-setup.md](docs/monitoring-setup.md) for security considerations

## Usage
Detailed usage instructions and examples can be found in the `docs/` directory.

## Contributing
Please read `docs/CONTRIBUTING.md` for details on our code of conduct and the process for submitting pull requests.

## License
This project is licensed under the MIT License - see the `docs/LICENSE` file for details.

## Documentation
- [Setup Guide](docs/setup.md)
- [Monitoring Setup Guide](docs/monitoring-setup.md)
- [Load Testing Guide](docs/load-testing.md)
- [Resource Sizing Guide](docs/resource-sizing.md)
- [API Reference](docs/api-reference.md)
