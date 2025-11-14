# Guía de Uso de JaCoCo para Code Coverage

## Comandos Disponibles

### 1. Ejecutar tests y generar reporte de coverage

```bash
cd services/orders-service
./../../gradlew test jacocoTestReport
```

Este comando:
- Ejecuta todos los tests unitarios
- Genera el reporte de cobertura en formato HTML y XML

### 2. Ver el reporte HTML

Después de ejecutar el comando anterior, abre el archivo HTML en tu navegador:

```bash
# En macOS
open build/reports/jacoco/test/html/index.html

# En Linux
xdg-open build/reports/jacoco/test/html/index.html

# En Windows
start build/reports/jacoco/test/html/index.html
```

### 3. Ver resumen en consola

El reporte se genera automáticamente después de ejecutar los tests. Para ver el resumen, revisa el reporte HTML.

### 4. Verificar reglas de coverage

```bash
./../../gradlew jacocoTestCoverageVerification
```

Esta tarea verifica que el coverage cumpla con las reglas definidas.

## Ubicación de los Reportes

- **HTML**: `build/reports/jacoco/test/html/index.html`
- **XML**: `build/reports/jacoco/test/jacocoTestReport.xml`
- **Datos de ejecución**: `build/jacoco/test.exec`

## Interpretación del Coverage

El reporte muestra:
- **INSTRUCTION**: Cobertura de instrucciones (líneas de código ejecutadas)
- **BRANCH**: Cobertura de ramas (if/else, switch, etc.)
- **LINE**: Cobertura de líneas
- **METHOD**: Cobertura de métodos
- **CLASS**: Cobertura de clases

## Nota

Si el reporte se salta (SKIPPED), puede ser porque:
1. No hay archivos de ejecución (.exec) generados
2. Spring Boot puede estar deshabilitando JaCoCo en algunos casos

En ese caso, ejecuta:
```bash
./../../gradlew clean test --rerun-tasks jacocoTestReport
```

