# 🌌 Nébula — Rede Social para Android

Rede social simples para Android onde o usuário cria uma conta, monta seu perfil
e compartilha fotos com legenda em um feed em tempo real.

Construído com **Kotlin, Jetpack Compose, Hilt e Firebase**.

---

## ✨ Funcionalidades e critérios atendidos

| Critério | Onde está implementado |
|---|---|
| **Autenticação de Usuário** | `AuthRemoteSource`, `AuthRepository`, telas `LoginScreen`/`RegisterScreen` — cadastro, login e gerenciamento de sessão (Firebase Authentication, e-mail/senha). A `SplashScreen` verifica a sessão atual e redireciona automaticamente. |
| **Perfil do Usuário** | `AuthRepository.register()` cria o documento em `profiles/{uid}` no Firestore no exato momento do cadastro. `ProfileScreen` + `ProfileViewModel` exibem e editam nome, e-mail, bio e foto. |
| **Feed de Publicações** | `PublicationRemoteSource.observePublications()` usa `addSnapshotListener` do Firestore, expondo um `Flow` que atualiza a tela automaticamente quando qualquer usuário publica algo novo. |
| **Criação de Publicações** | `CreatePublicationScreen` escolhe uma imagem da galeria, `ImageCompressor` comprime, `PublicationRepository.createPublication()` sobe a imagem para o Cloud Storage e grava o post no Firestore. |

---

## 🛠️ Tecnologias

- **Linguagem:** 100% Kotlin
- **UI:** Jetpack Compose + Material 3
- **Arquitetura:** MVVM
- **Injeção de dependência:** Hilt
- **Backend:** Firebase (Authentication, Cloud Firestore, Cloud Storage)
- **Carregamento de imagens:** Coil

---

## 🚀 Como executar

1. **Crie um projeto no Firebase** (console.firebase.google.com), mantendo o
   plano **Spark (gratuito)** ativo para avaliação.
2. **Adicione um app Android** ao projeto Firebase com o pacote:
   ```
   com.duo.nebula
   ```
3. Baixe o `google-services.json` gerado e coloque-o dentro da pasta `app/`.
4. No console do Firebase, ative:
   - **Authentication** → método de login "E-mail/senha"
   - **Cloud Firestore** (modo produção)
   - **Cloud Storage**
5. Publique as regras de segurança já incluídas no projeto:
   ```
   firebase deploy --only firestore:rules,storage:rules
   ```
   (ou cole o conteúdo de `firestore.rules` e `storage.rules` diretamente no console)
6. Abra o projeto no Android Studio e rode em um emulador ou dispositivo físico.

---

## 📁 Estrutura de dados no Firestore

- `profiles/{uid}` → `uid`, `displayName`, `email`, `bio`, `photoUrl`, `createdAt`
- `publications/{id}` → `authorId`, `caption`, `imageUrl`, `createdAt`

---

Projeto desenvolvido como estudo de arquitetura moderna Android com Firebase.
