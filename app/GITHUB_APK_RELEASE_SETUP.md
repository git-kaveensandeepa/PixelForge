# PixelForge - Automated GitHub Actions & APK Release Setup

This guide explains how to automatically build APK files and publish GitHub Releases for your **PixelForge** Android project using **GitHub Actions**.

---

## Step 1: Push Your Project to GitHub

1. Create a new repository on [GitHub](https://github.com/new) (e.g., `PixelForge`).
2. Initialize and push your existing project to your GitHub repository:
   ```bash
   git init
   git add .
   git commit -m "Initial commit with PixelForge Android project & GitHub Actions"
   git branch -M main
   git remote add origin https://github.com/YOUR_USERNAME/PixelForge.git
   git push -u origin main
   ```

---

## Step 2: Enable GitHub Actions Permissions

1. Go to your repository on GitHub.
2. Navigate to **Settings** > **Actions** > **General**.
3. Scroll down to **Workflow permissions**.
4. Select **Read and write permissions** so that GitHub Actions can automatically create releases and attach APK assets.
5. Click **Save**.

---

## Step 3: Automated APK Builds on Every Push

The workflow file `.github/workflows/android-apk.yml` is already configured in your project.
- Whenever you push code to `main` or `master`, GitHub Actions will automatically:
  1. Set up a Java JDK 17 environment.
  2. Build the debug APK using `./gradlew assembleDebug`.
  3. Upload the APK as a downloadable artifact named `PixelForge-debug.apk`.

To download your built APK:
1. Go to your GitHub repository and click on the **Actions** tab.
2. Click on the latest workflow run.
3. Scroll down to the **Artifacts** section and click **PixelForge-debug.apk** to download it directly to your phone or computer.

---

## Step 4: Publishing Automated GitHub Releases via Tags

When you are ready to release a stable version of PixelForge:

1. Create and push a version tag (e.g., `v1.0.0`):
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```
2. GitHub Actions will automatically:
   - Build the release APK (`assembleRelease` or fallback debug build).
   - Generate release notes.
   - Create a GitHub Release corresponding to tag `v1.0.0`.
   - Attach the APK file directly to the release assets for instant download by users!

---

## Step 5: Manual Workflow Trigger (`workflow_dispatch`)

You can also trigger an APK build manually at any time without pushing code:
1. Go to **Actions** in your GitHub repository.
2. Select the **Build Android APK & Release** workflow from the left sidebar.
3. Click **Run workflow** dropdown and click the green **Run workflow** button.
