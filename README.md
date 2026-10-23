# TPO Programación III — Red social universitaria

**Equipo: los aJAVAdos**

| Integrante | GitHub |
|---|---|
| Alexis | [@Alexis-9](https://github.com/Alexis-9) |
| Ralph | [@ralphjacoby1-web](https://github.com/ralphjacoby1-web) |
| Martín | [@mstockl9](https://github.com/mstockl9) |

Propuesta elegida: **Propuesta 1 — Red social universitaria para la colaboración**.

La plataforma administra usuarios, publicaciones, vínculos entre usuarios e iniciativas universitarias, y resuelve cada funcionalidad con una estrategia de diseño de algoritmos vista en la materia.

---

## Funcionalidades de la primera etapa

| # | Funcionalidad | Estrategia | Algoritmo |
|---|---|---|---|
| 1 | Organización del muro de publicaciones | Divide y Conquista | MergeSort genérico y estable |
| 2 | Cadena de difusión más rápida | Greedy | Dijkstra (versión de la Clase 4) |
| 3 | Selección de iniciativas para financiar | Programación Dinámica | Mochila 0/1 con reconstrucción |

### 1. Muro de publicaciones
Ordena las publicaciones por fecha (más reciente primero) o por relevancia (`reacciones + 3 × comentarios`). En los empates gana la más reciente y después el id menor. No usa métodos de ordenamiento nativos; se implementa mediante algoritmos sobre colecciones.

### 2. Cadena de difusión más rápida
Dados un origen y un destino, calcula la cadena de menor tiempo total, los usuarios que la forman y el tiempo en minutos. Los vínculos son dirigidos y tienen tiempo positivo. Si el destino no es alcanzable, informa la situación sin errores.

### 3. Selección de iniciativas
Con un presupuesto limitado, elige el conjunto de iniciativas de mayor impacto total sin superarlo. Cada iniciativa se financia completa y una sola vez. Muestra las iniciativas elegidas, el costo total y el beneficio acumulado.

---

## Segunda etapa (a confirmar)

- **A1** — Consultas de difusión entre todos los usuarios
- **B3** — Cadenas alternativas de difusión
- **B4** — Asignación de moderadores

---

## Estructura del proyecto

```text
src/
├── main/
│   ├── java/
│   │   ├── Main.java
│   │   ├── algoritmos/
│   │   │   ├── ...
│   │   ├── datos/
│   │   │   ├── DatosDemo.java
│   │   │   └── Plataforma.java
│   │   ├── model/
│   │   │   ├── ...
│   │   ├── service/
│   │   │   ├── ...
│   │   ├── view/
│   │   │   └── MenuConsola.java
│   │   └── web/
│   │       ├── ApiServer.java
│   │       └── controller/
│   └── resources/
│       └── ...
└── test/
    └── java/
        ├── algoritmos/
        ├── datos/
        └── service/
```

---

## Cómo ejecutarlo

Requisitos: Java 17+ y Maven.

```bash
# Compilar y correr los tests
mvn test

# Levantar la aplicación web
mvn compile exec:java
```

Después abrir `http://localhost:7070` en el navegador. La aplicación arranca con los datos de `DatosDemo` y desde el frontend se pueden cargar y borrar usuarios, vínculos, publicaciones e iniciativas.

---

## Tests

Los tests JUnit cubren los services de las tres funcionalidades:

- `MuroServiceTest` — orden por fecha y por relevancia, desempates
- `DifusionServiceTest` — cadena mínima, destino inalcanzable, traza de decisiones
- `IniciativasServiceTest` — selección óptima, presupuesto insuficiente, caso Greedy vs. PD

---
