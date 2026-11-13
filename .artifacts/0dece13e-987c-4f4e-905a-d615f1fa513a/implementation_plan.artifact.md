# Implementation Plan - Local Media Remodeling for All Entities

Remodel the app's data layer and media handling so that all images (Recipes, User profile, and Shopping Carts) are downloaded and stored locally in internal storage, with dedicated Room database columns, automated migration/recovery of remote images, and cleanup of records without valid local media.

## User Review Required

> [!IMPORTANT]
> This change modifies Room database entities (`Recipe`, `User`, `Cart`) by adding new local path columns and migrating database version to 21. Records whose media cannot be retrieved or downloaded locally will be cleaned up.

## Proposed Changes

### 1. Data Models (`Recipe`, `User`, `Cart`)
#### [MODIFY] [Recipe.kt](file:///C:/Users/BemTeVi/StudioProjects/Top-chef/app/src/main/java/com/sam/topchef/core/data/model/Recipe.kt)
- Add `localImagePaths: List<String>` column.
- Add helper method `getEffectiveImageUris()` to prefer local files over remote URLs.

#### [MODIFY] [User.kt](file:///C:/Users/BemTeVi/StudioProjects/Top-chef/app/src/main/java/com/sam/topchef/core/data/model/User.kt)
- Add `localImageUri: String?` column.
- Add helper method `getEffectiveImageUri()`.

#### [MODIFY] [Cart.kt](file:///C:/Users/BemTeVi/StudioProjects/Top-chef/app/src/main/java/com/sam/topchef/core/data/model/Cart.kt)
- Add `localCartImage: String?` column.
- Add helper method `getEffectiveCartImage()`.

### 2. Database & Migrations
#### [MODIFY] [DataBaseMigration.kt](file:///C:/Users/BemTeVi/StudioProjects/Top-chef/app/src/main/java/com/sam/topchef/core/data/local/migration/DataBaseMigration.kt)
- Add `MIGRATION_20_21` adding columns:
  - `Recipe`: `local_image_paths` (TEXT)
  - `User`: `local_image_uri` (TEXT)
  - `Cart`: `local_cart_image` (TEXT)

#### [MODIFY] [AppDataBase.kt](file:///C:/Users/BemTeVi/StudioProjects/Top-chef/app/src/main/java/com/sam/topchef/core/data/local/appDataBase/AppDataBase.kt)
- Bump database version to 21.
- Register `MIGRATION_20_21`.

### 3. Media Downloader & Migration Utility
#### [NEW] [MediaDownloader.kt](file:///C:/Users/BemTeVi/StudioProjects/Top-chef/app/src/main/java/com/sam/topchef/core/utils/MediaDownloader.kt)
- Utility class to download remote/URI images to internal storage (`filesDir/recipe_images`, `filesDir/user_images`, `filesDir/cart_images`).
- Automatically cleans up records that fail media retrieval or lack local paths.

### 4. UI & Activity Integration
- Update recipe creation, editing, profile management, and shopping cart flows to utilize local media paths and download remote assets upon insertion.

## Verification Plan

### Automated Tests
- Run Gradle build (`app:assembleDebug`) to verify compilation and schema correctness.

### Manual Verification
- Test adding/editing a recipe, updating user profile picture, and creating shopping carts, verifying images are stored locally in internal storage and persist correctly.
