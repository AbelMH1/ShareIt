# 📸 ShareIt – Aplicación Android para la compartición y gestión de álbumes de fotos entre múltiples usuarios.

Aplicación móvil desarrollada en Kotlin que permite a los usuarios crear, compartir y colaborar en álbumes de fotos asociados a eventos como viajes, celebraciones o encuentros sociales. ShareIt combina funcionalidades de red social, privacidad configurable y experiencia visual atractiva.

---

## ✨ Características principales

- 📁 Creación y edición de álbumes privados, compartidos o públicos
- 👥 Gestión de participantes y permisos por roles
- 📷 Subida de imágenes desde cámara o galería
- 💬 Chat integrado por álbum
- 💡 Valoración y exploración de imágenes (likes, filtros, ordenación)
- 🌐 Exploración de álbumes públicos
- 🌙 Tema claro/oscuro e internacionalización (ES/EN)
- 🔐 Autenticación con correo o cuenta de Google

---

## ⚙️ Instalación y ejecución

### 🙋‍♂️ Para usuarios
Ir a la sección [Releases](https://github.com/AbelMH1/ShareIt/releases) y descargar la APK de la versión más reciente en un dispositivo Android 8.0 o superior. Ejecutar APK para completar la instalación en el dispositivo.

### 👨‍💻 Para desarrolladores
#### Requisitos previos

- [Android Studio](https://developer.android.com/studio?hl=es-419)
- JDK 17
- Cuenta de Firebase (para configurar los servicios)
- Dispositivo/emulador Android 8.0 o superior (API 26+)

#### Clonación y compilación

```bash
git clone https://github.com/AbelMH1/ShareIt.git
cd ShareIt
./gradlew build
```

Importa el proyecto en Android Studio y ejecuta en un emulador o dispositivo físico.

---

## 🎮 Cómo usar la aplicación

1. Regístrate o inicia sesión con Google
2. Crea un álbum nuevo (público, compartido o privado)
3. Añade imágenes desde tu galería o cámara
4. Invita participantes o permite que se unan automáticamente (modo público)
5. Explora, comenta, valora y chatea en álbumes colaborativos
6. Filtra y ordena imágenes o álbumes por nombre, fecha o popularidad

---

## 📱 Capturas de pantalla

<p align="center">
  <img src="https://github.com/user-attachments/assets/c3f62f4f-109a-45c5-b9a0-779c102eae59" width="200" alt="login">
  <img src="https://github.com/user-attachments/assets/c130c068-0afc-4cc3-a22e-43e9e3343d93" width="200" alt="albums">
  <img src="https://github.com/user-attachments/assets/8aa4b93b-f9ef-4921-b46b-05ebf648f586" width="200" alt="images">
  <img src="https://github.com/user-attachments/assets/9ab98a0d-1b36-4f14-bd16-b1f35964da14" width="200" alt="image details">
  <img src="https://github.com/user-attachments/assets/528a9c6f-c800-40b0-ab3b-a2416ebb1ffd" width="200" alt="add image">
  <img src="https://github.com/user-attachments/assets/cea75297-7711-441e-840b-7a01af3d48f6" width="200" alt="chat">
  <img src="https://github.com/user-attachments/assets/ff42e070-2829-4b83-b8ee-fd37498b50b6" width="200" alt="shared settings">
  <img src="https://github.com/user-attachments/assets/8c6e2346-b045-426c-afc4-0d8b42a98a93" width="200" alt="participants">
  <img src="https://github.com/user-attachments/assets/b6c832a3-b38f-40de-8c0e-082a5ce8b3f9" width="200" alt="menu">
  <img src="https://github.com/user-attachments/assets/3d6c3635-352a-4c54-9762-f7a968a03fef" width="200" alt="profile">
  <img src="https://github.com/user-attachments/assets/d4140055-79aa-40e7-ba19-bee5e161dbe6" width="200" alt="explore">
  <img src="https://github.com/user-attachments/assets/c1d2a186-614e-4a9a-bb44-b91bc92d4320" width="200" alt="explore search">
</p>

---

## 🛠️ Tecnologías y arquitectura

- **Lenguaje:** Kotlin
- **Arquitectura:** Model-View-ViewModel (MVVM)
- **Interfaz:** IU basadas en vistas XML, Jetpack ViewBinding, Material Design 3
- **Persistencia:** Firebase Firestore
- **Multimedia:** Firebase Storage
- **Autenticación:** Firebase Authentication (correo + Google)
- **Backend Serverless:** Firebase Cloud Functions

---

## 📂 Estructura del proyecto

```
ShareIt/
├── app/
│   ├── src/main/
│   │   ├── java/com/shareit/
│   │   │   ├── view/           # Fragments y UI
│   │   │   ├── viewmodel/      # ViewModels por módulo
│   │   │   ├── model/          # Clases de dominio y repositorios
│   │   │   └── service/        # Servicios Android
│   |   └── res/                # Layouts, temas y strings
|   └── build.gradle
└── build.gradle
```

---

## 📍 Estado del proyecto y mejoras futuras

Versión 1.0.1 – Proyecto finalizado y funcional

🔜 Posibles ampliaciones:

- Subida múltiple de imágenes
- Moderación de contenido automatizada (Azure AI)
- Notificaciones push (Firebase Cloud Messaging)
- Tests automatizados (JUnit + Espresso)
- Inyección de dependencias (Koin / Hilt)
- Álbumes tipo “Cápsula del tiempo”

---

## 👤 Autor

**Abel Menéndez Hernández**  
Trabajo Fin de Grado – Universidad de Oviedo  
