# Multilingual Generation System Design Document

## 1. Overview

### 1.1 Purpose
This document outlines the design of a **Multilingual Generation System** that enables automatic translation and localization of content across all front-end applications, including web and mobile platforms.

### 1.2 Problem Statement
Organizations managing multiple front-end applications face challenges in:
- Maintaining consistent translations across platforms
- Managing localization files in various formats
- Scaling multilingual support efficiently
- Reducing manual translation overhead

### 1.3 Solution
A centralized system that allows users to upload source content in one language and automatically generate localized materials in multiple target languages with customizable output formats.

---

## 2. System Architecture

### 2.1 High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           Client Applications                            │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐    │
│  │   Web App   │  │ Android App │  │   iOS App   │  │  Desktop    │    │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘    │
└─────────┼────────────────┼────────────────┼────────────────┼───────────┘
          │                │                │                │
          └────────────────┴────────────────┴────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                           API Gateway                                    │
│                    (Authentication & Rate Limiting)                      │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                      Multilingual Generation Service                     │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐         │
│  │  File Parser    │  │ Translation     │  │  File Generator │         │
│  │  Module         │  │ Engine          │  │  Module         │         │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘         │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
          ┌─────────────────────────┼─────────────────────────┐
          ▼                         ▼                         ▼
┌─────────────────┐      ┌─────────────────┐      ┌─────────────────┐
│   Translation   │      │    Database     │      │  File Storage   │
│   Provider API  │      │   (PostgreSQL)  │      │   (S3/GCS)      │
│  (AI/LLM Based) │      │                 │      │                 │
└─────────────────┘      └─────────────────┘      └─────────────────┘
```

### 2.2 Core Components

| Component | Description |
|-----------|-------------|
| **API Gateway** | Handles authentication, authorization, and request routing |
| **File Parser Module** | Parses uploaded files and extracts translatable content |
| **Translation Engine** | Integrates with AI/LLM services for translation |
| **File Generator Module** | Generates output files in specified formats |
| **Database** | Stores projects, translation memory, and user data |
| **File Storage** | Stores uploaded and generated files |

---

## 3. Features

### 3.1 Core Features

| Feature | Description |
|---------|-------------|
| **Single Source Upload** | Upload content in one source language |
| **Multi-Target Generation** | Generate translations for multiple languages simultaneously |
| **Format Selection** | Choose output file formats per target language |
| **Platform Support** | Support for web, Android, iOS, and desktop applications |
| **Translation Memory** | Store and reuse previous translations |
| **Batch Processing** | Process multiple files in a single operation |
| **Real-time Preview** | Preview translations before downloading |

### 3.2 Advanced Features

- **Context-Aware Translation**: AI-powered translations considering UI context
- **Glossary Management**: Custom terminology for brand consistency
- **Version Control**: Track changes across translation versions
- **Collaboration Tools**: Team-based review and approval workflows
- **API Integration**: REST/GraphQL APIs for CI/CD pipeline integration

---

## 4. Supported File Formats

### 4.1 Input Formats

| Platform | Format | Extension | Description |
|----------|--------|-----------|-------------|
| Android | XML Resources | `.xml` | Android string resources |
| iOS | Strings | `.strings` | iOS localization strings |
| iOS | Stringsdict | `.stringsdict` | iOS pluralization strings |
| Web | JSON | `.json` | Key-value JSON format |
| Web | Properties | `.properties` | Java properties format |
| Universal | XLIFF | `.xliff` | XML Localization Interchange |
| Universal | PO/POT | `.po` / `.pot` | GNU gettext format |
| Universal | CSV | `.csv` | Spreadsheet format |
| Universal | YAML | `.yaml` / `.yml` | YAML format |

### 4.2 Output Formats

```
┌────────────────────────────────────────────────────────────────┐
│                     Output Format Matrix                        │
├──────────────┬─────────────────────────────────────────────────┤
│   Platform   │            Supported Output Formats              │
├──────────────┼─────────────────────────────────────────────────┤
│   Android    │  XML (.xml), JSON (.json), XLIFF (.xliff)       │
├──────────────┼─────────────────────────────────────────────────┤
│     iOS      │  .strings, .stringsdict, XLIFF (.xliff)         │
├──────────────┼─────────────────────────────────────────────────┤
│     Web      │  JSON (.json), JS (.js), TS (.ts), YAML (.yml)  │
├──────────────┼─────────────────────────────────────────────────┤
│   Flutter    │  ARB (.arb), JSON (.json)                       │
├──────────────┼─────────────────────────────────────────────────┤
│  React Native│  JSON (.json), JS (.js)                         │
├──────────────┼─────────────────────────────────────────────────┤
│     KMP      │  XML (.xml), JSON (.json), Properties           │
└──────────────┴─────────────────────────────────────────────────┘
```

---

## 5. API Design

### 5.1 REST API Endpoints

#### Upload Source File
```http
POST /api/v1/projects/{projectId}/upload
Content-Type: multipart/form-data

Request Body:
- file: <source_file>
- sourceLanguage: "en"
```

#### Generate Translations
```http
POST /api/v1/projects/{projectId}/generate
Content-Type: application/json

{
  "sourceFileId": "file_123",
  "targetLanguages": ["zh-CN", "ja", "ko", "es", "fr"],
  "outputFormats": {
    "zh-CN": ["xml", "json"],
    "ja": ["strings", "json"],
    "ko": ["xml"],
    "es": ["json"],
    "fr": ["json", "yaml"]
  },
  "options": {
    "useTranslationMemory": true,
    "preservePlaceholders": true,
    "contextHints": "mobile_app_ui"
  }
}
```

#### Download Generated Files
```http
GET /api/v1/projects/{projectId}/downloads/{generationId}

Response:
- ZIP file containing all generated language files
```

### 5.2 Response Structure

```json
{
  "success": true,
  "data": {
    "generationId": "gen_456",
    "status": "completed",
    "sourceFile": {
      "id": "file_123",
      "name": "strings.xml",
      "language": "en",
      "keyCount": 150
    },
    "generatedFiles": [
      {
        "language": "zh-CN",
        "format": "xml",
        "downloadUrl": "/downloads/gen_456/zh-CN/strings.xml",
        "keyCount": 150,
        "translatedCount": 148,
        "reviewRequired": 2
      }
    ],
    "metadata": {
      "createdAt": "2025-12-18T10:30:00Z",
      "completedAt": "2025-12-18T10:32:15Z",
      "processingTimeMs": 135000
    }
  }
}
```

---

## 6. Mobile Platform Support

### 6.1 Android Support

#### Input: `strings.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">My Application</string>
    <string name="welcome_message">Welcome, %1$s!</string>
    <plurals name="items_count">
        <item quantity="one">%d item</item>
        <item quantity="other">%d items</item>
    </plurals>
</resources>
```

#### Output Structure
```
res/
├── values/
│   └── strings.xml (source - English)
├── values-zh-rCN/
│   └── strings.xml (Chinese Simplified)
├── values-ja/
│   └── strings.xml (Japanese)
└── values-ko/
    └── strings.xml (Korean)
```

### 6.2 iOS Support

#### Input: `Localizable.strings`
```
/* Welcome screen title */
"welcome_title" = "Welcome";

/* Greeting message with username parameter */
"welcome_message" = "Hello, %@!";
```

#### Output Structure
```
Localization/
├── en.lproj/
│   └── Localizable.strings (source - English)
├── zh-Hans.lproj/
│   └── Localizable.strings (Chinese Simplified)
├── ja.lproj/
│   └── Localizable.strings (Japanese)
└── ko.lproj/
    └── Localizable.strings (Korean)
```

### 6.3 Cross-Platform (Flutter/KMP)

#### Flutter ARB Format
```json
{
  "@@locale": "en",
  "welcomeTitle": "Welcome",
  "@welcomeTitle": {
    "description": "Title shown on welcome screen"
  },
  "itemCount": "{count, plural, =0{No items} =1{1 item} other{{count} items}}",
  "@itemCount": {
    "placeholders": {
      "count": {
        "type": "int"
      }
    }
  }
}
```

---

## 7. Web Interface

### 7.1 Web Dashboard Overview

The web interface provides a user-friendly platform for uploading source files and downloading generated multilingual files.

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        Web Dashboard Layout                              │
├─────────────────────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                    Navigation Bar                                │   │
│  │  [Logo]  Projects  History  Settings  [User Profile]            │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                         │
│  ┌───────────────────────────┐  ┌───────────────────────────────────┐  │
│  │     File Upload Area      │  │       Generation Settings         │  │
│  │  ┌─────────────────────┐  │  │                                   │  │
│  │  │                     │  │  │  Source Language: [English ▼]     │  │
│  │  │   Drop files here   │  │  │                                   │  │
│  │  │        or           │  │  │  Target Languages:                │  │
│  │  │   [Browse Files]    │  │  │  ☑ Chinese (Simplified)          │  │
│  │  │                     │  │  │  ☑ Japanese                       │  │
│  │  └─────────────────────┘  │  │  ☑ Korean                         │  │
│  │                           │  │  ☐ Spanish                        │  │
│  │  Supported: .xml, .json,  │  │  ☐ French                         │  │
│  │  .strings, .arb, .yaml    │  │                                   │  │
│  └───────────────────────────┘  │  Output Format: [XML ▼]           │  │
│                                 │                                   │  │
│                                 │  [Generate Translations]          │  │
│                                 └───────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────┘
```

### 7.2 File Upload Interface

#### Upload Methods
| Method | Description |
|--------|-------------|
| **Drag & Drop** | Drag files directly onto the upload zone |
| **File Browser** | Click to open system file picker |
| **URL Import** | Import from external URL (GitHub, GitLab, etc.) |
| **Paste Content** | Paste text content directly into editor |

#### Upload Flow

```
┌──────────────────────────────────────────────────────────────────────┐
│                      File Upload Process                              │
└──────────────────────────────────────────────────────────────────────┘

     User Drops File          System Validates         File Parsed
          │                         │                       │
          ▼                         ▼                       ▼
    ┌──────────┐             ┌──────────┐             ┌──────────┐
    │  Select  │────────────▶│ Validate │────────────▶│  Parse   │
    │   File   │             │  Format  │             │   Keys   │
    └──────────┘             └──────────┘             └──────────┘
                                   │                       │
                                   ▼                       ▼
                            ┌──────────┐             ┌──────────┐
                            │  Error   │             │  Preview │
                            │ Message  │             │ Content  │
                            └──────────┘             └──────────┘
```

#### Upload Component Features

```html
<!-- Web Upload Component Structure -->
<div class="upload-container">
  <!-- Drag & Drop Zone -->
  <div class="dropzone" 
       ondragover="handleDragOver()" 
       ondrop="handleFileDrop()">
    <icon>cloud_upload</icon>
    <p>Drag and drop your localization file here</p>
    <p>or</p>
    <button class="browse-btn">Browse Files</button>
    <input type="file" accept=".xml,.json,.strings,.arb,.yaml,.properties" />
  </div>
  
  <!-- Upload Progress -->
  <div class="progress-bar">
    <div class="progress" style="width: 0%"></div>
  </div>
  
  <!-- File Info Preview -->
  <div class="file-preview">
    <span class="filename">strings.xml</span>
    <span class="filesize">24 KB</span>
    <span class="key-count">156 keys detected</span>
  </div>
</div>
```

### 7.3 Generation Configuration Panel

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    Translation Configuration                             │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  📁 Source File: strings.xml (156 keys)                                 │
│  🌐 Source Language: English (en)                     [Auto-Detected]   │
│                                                                          │
│  ─────────────────────────────────────────────────────────────────────  │
│                                                                          │
│  Target Languages & Formats:                                             │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │ ☑ Chinese (Simplified)  │  Format: [XML ▼] [JSON ▼] [+ Add]       │ │
│  │ ☑ Japanese              │  Format: [XML ▼]                         │ │
│  │ ☑ Korean                │  Format: [XML ▼] [Strings ▼]            │ │
│  │ ☑ Spanish               │  Format: [JSON ▼]                        │ │
│  │ ☐ French                │  Format: [─────]                         │ │
│  │ ☐ German                │  Format: [─────]                         │ │
│  │                                                                    │ │
│  │ [+ Add More Languages]                                             │ │
│  └────────────────────────────────────────────────────────────────────┘ │
│                                                                          │
│  Advanced Options:                                                       │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │ ☑ Use Translation Memory      ☐ Skip already translated keys      │ │
│  │ ☑ Preserve placeholders       ☐ Generate plural forms              │ │
│  │ ☐ Include comments            ☐ Sort keys alphabetically           │ │
│  └────────────────────────────────────────────────────────────────────┘ │
│                                                                          │
│            [Cancel]                    [🚀 Generate Translations]        │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

### 7.4 Download Interface

#### Download Center

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        Download Center                                   │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  Generation ID: gen_456                        Status: ✅ Completed      │
│  Generated: Dec 18, 2025, 10:32 AM             Duration: 2m 15s         │
│                                                                          │
│  ─────────────────────────────────────────────────────────────────────  │
│                                                                          │
│  📦 Download All Files                                    [⬇ ZIP 48KB]  │
│                                                                          │
│  ─────────────────────────────────────────────────────────────────────  │
│                                                                          │
│  Individual Files:                                                       │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │ 🇨🇳 Chinese (Simplified)                                           │ │
│  │    ├─ strings.xml (XML)         12 KB    [👁 Preview] [⬇ Download] │ │
│  │    └─ strings.json (JSON)        8 KB    [👁 Preview] [⬇ Download] │ │
│  │                                                                    │ │
│  │ 🇯🇵 Japanese                                                        │ │
│  │    └─ strings.xml (XML)         14 KB    [👁 Preview] [⬇ Download] │ │
│  │                                                                    │ │
│  │ 🇰🇷 Korean                                                          │ │
│  │    ├─ strings.xml (XML)         11 KB    [👁 Preview] [⬇ Download] │ │
│  │    └─ Localizable.strings        9 KB    [👁 Preview] [⬇ Download] │ │
│  │                                                                    │ │
│  │ 🇪🇸 Spanish                                                         │ │
│  │    └─ strings.json (JSON)        7 KB    [👁 Preview] [⬇ Download] │ │
│  └────────────────────────────────────────────────────────────────────┘ │
│                                                                          │
│  [📋 Copy API Endpoint]    [🔄 Regenerate]    [📤 Share Link]           │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

#### Download Options

| Option | Description |
|--------|-------------|
| **Download All (ZIP)** | Download all generated files in a single ZIP archive |
| **Individual Download** | Download specific language/format files separately |
| **Preview** | View file contents in browser before downloading |
| **Copy API Endpoint** | Get the direct API URL for programmatic access |
| **Share Link** | Generate a shareable link for team members |

### 7.5 File Preview Modal

```
┌─────────────────────────────────────────────────────────────────────────┐
│  Preview: values-zh-rCN/strings.xml                           [✕ Close]│
├─────────────────────────────────────────────────────────────────────────┤
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │ <?xml version="1.0" encoding="utf-8"?>                             │ │
│  │ <resources>                                                        │ │
│  │     <string name="app_name">我的应用</string>                       │ │
│  │     <string name="welcome_message">欢迎，%1$s！</string>            │ │
│  │     <string name="login_button">登录</string>                       │ │
│  │     <string name="signup_button">注册</string>                      │ │
│  │     <plurals name="items_count">                                   │ │
│  │         <item quantity="other">%d 个项目</item>                     │ │
│  │     </plurals>                                                     │ │
│  │ </resources>                                                       │ │
│  └────────────────────────────────────────────────────────────────────┘ │
│                                                                          │
│  Keys: 156  │  Translated: 154  │  Review Needed: 2                     │
│                                                                          │
│         [📋 Copy Content]              [⬇ Download File]                │
└─────────────────────────────────────────────────────────────────────────┘
```

### 7.6 Generation History

```
┌─────────────────────────────────────────────────────────────────────────┐
│                      Generation History                                  │
├─────────────────────────────────────────────────────────────────────────┤
│  🔍 Search...                           Filter: [All ▼]  [This Week ▼] │
│                                                                          │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │ Date          │ Source File    │ Languages │ Status   │ Actions   │ │
│  ├───────────────┼────────────────┼───────────┼──────────┼───────────┤ │
│  │ Dec 18, 10:32 │ strings.xml    │ 4 langs   │ ✅ Done  │ [⬇] [🔄] │ │
│  │ Dec 17, 15:20 │ en.json        │ 6 langs   │ ✅ Done  │ [⬇] [🔄] │ │
│  │ Dec 17, 09:15 │ Localizable    │ 3 langs   │ ✅ Done  │ [⬇] [🔄] │ │
│  │ Dec 16, 14:45 │ messages.yaml  │ 8 langs   │ ⚠️ Partial│ [⬇] [🔄] │ │
│  │ Dec 15, 11:30 │ strings.xml    │ 2 langs   │ ❌ Failed │ [──] [🔄] │ │
│  └────────────────────────────────────────────────────────────────────┘ │
│                                                                          │
│  Showing 5 of 24 generations                    [< Prev] [1] [2] [Next >]│
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 8. User Flow

### 8.1 Standard Workflow

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         User Workflow                                    │
└─────────────────────────────────────────────────────────────────────────┘

   ┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐
   │  Upload  │────▶│  Select  │────▶│ Generate │────▶│ Download │
   │  Source  │     │ Languages│     │   Files  │     │  Output  │
   └──────────┘     └──────────┘     └──────────┘     └──────────┘
        │                │                │                │
        ▼                ▼                ▼                ▼
   ┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐
   │ Parse &  │     │  Choose  │     │ AI Trans │     │  Export  │
   │ Validate │     │ Formats  │     │ + Review │     │   ZIP    │
   └──────────┘     └──────────┘     └──────────┘     └──────────┘
```

### 7.2 Detailed Steps

1. **Upload Source Material**
   - User uploads a localization file (e.g., `strings.xml`, `en.json`)
   - System validates file format and extracts keys/values
   - Source language is detected or specified

2. **Configure Target Languages**
   - Select desired target languages from supported list
   - Optionally configure per-language output formats

3. **Choose Output Formats**
   - Select file formats for each target language
   - Configure platform-specific options

4. **Generate Translations**
   - AI-powered translation engine processes content
   - Translation memory applied for consistency
   - Quality checks performed

5. **Review & Download**
   - Preview translations in web interface
   - Make manual adjustments if needed
   - Download as individual files or bundled ZIP

---

## 8. Supported Languages

### 8.1 Tier 1 Languages (Full Support)
| Code | Language | Native Name |
|------|----------|-------------|
| en | English | English |
| zh-CN | Chinese (Simplified) | 简体中文 |
| zh-TW | Chinese (Traditional) | 繁體中文 |
| ja | Japanese | 日本語 |
| ko | Korean | 한국어 |
| es | Spanish | Español |
| fr | French | Français |
| de | German | Deutsch |
| pt-BR | Portuguese (Brazil) | Português |
| ar | Arabic | العربية |

### 8.2 Tier 2 Languages (Standard Support)
- Italian (it)
- Russian (ru)
- Dutch (nl)
- Polish (pl)
- Turkish (tr)
- Vietnamese (vi)
- Thai (th)
- Indonesian (id)
- Hindi (hi)
- And 50+ additional languages

---

## 9. Technical Implementation

### 9.1 Technology Stack

| Layer | Technology |
|-------|------------|
| **Backend** | Kotlin/Spring Boot or Node.js |
| **Database** | PostgreSQL + Redis (caching) |
| **File Storage** | AWS S3 / Google Cloud Storage |
| **Translation AI** | OpenAI GPT / Google Cloud Translation / DeepL |
| **Message Queue** | RabbitMQ / Apache Kafka |
| **Frontend** | React / Vue.js |
| **Mobile SDK** | Kotlin Multiplatform / Swift |

### 9.2 Data Model

```
┌─────────────────┐       ┌─────────────────┐       ┌─────────────────┐
│     Project     │       │   SourceFile    │       │  Translation    │
├─────────────────┤       ├─────────────────┤       ├─────────────────┤
│ id              │──────▶│ id              │──────▶│ id              │
│ name            │       │ projectId       │       │ sourceFileId    │
│ ownerId         │       │ filename        │       │ targetLanguage  │
│ defaultLanguage │       │ sourceLanguage  │       │ key             │
│ createdAt       │       │ format          │       │ value           │
│ updatedAt       │       │ keyCount        │       │ status          │
└─────────────────┘       │ uploadedAt      │       │ confidence      │
                          └─────────────────┘       │ reviewedBy      │
                                                    │ updatedAt       │
                                                    └─────────────────┘
```

### 9.3 Translation Memory Schema

```sql
CREATE TABLE translation_memory (
    id UUID PRIMARY KEY,
    project_id UUID REFERENCES projects(id),
    source_language VARCHAR(10) NOT NULL,
    target_language VARCHAR(10) NOT NULL,
    source_text TEXT NOT NULL,
    translated_text TEXT NOT NULL,
    context VARCHAR(255),
    confidence_score DECIMAL(3,2),
    usage_count INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    
    UNIQUE(project_id, source_language, target_language, source_text)
);
```

---

## 10. Security Considerations

### 10.1 Authentication & Authorization
- OAuth 2.0 / JWT-based authentication
- Role-based access control (RBAC)
- API key management for CI/CD integration

### 10.2 Data Protection
- End-to-end encryption for file transfers
- Encryption at rest for stored files
- GDPR compliance for user data
- Data retention policies

### 10.3 Rate Limiting
| Tier | Requests/Hour | File Size Limit | Languages/Request |
|------|---------------|-----------------|-------------------|
| Free | 100 | 1 MB | 3 |
| Pro | 1,000 | 10 MB | 10 |
| Enterprise | Unlimited | 100 MB | Unlimited |

---

## 11. Integration Guide

### 11.1 CI/CD Pipeline Integration

```yaml
# GitHub Actions Example
name: Localization Update

on:
  push:
    paths:
      - 'app/src/main/res/values/strings.xml'

jobs:
  generate-translations:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Generate Translations
        uses: multilingual-system/action@v1
        with:
          api-key: ${{ secrets.ML_API_KEY }}
          source-file: 'app/src/main/res/values/strings.xml'
          source-language: 'en'
          target-languages: 'zh-CN,ja,ko,es,fr'
          output-format: 'xml'
          
      - name: Create PR with translations
        uses: peter-evans/create-pull-request@v5
        with:
          title: 'Update translations'
          branch: 'translations/auto-update'
```

### 11.2 SDK Integration (Android)

```kotlin
// Initialize the SDK
val mlClient = MultilingualClient.Builder()
    .apiKey("your-api-key")
    .projectId("project-123")
    .build()

// Upload and generate translations
lifecycleScope.launch {
    val result = mlClient.generateTranslations(
        sourceFile = File("strings.xml"),
        sourceLanguage = "en",
        targetLanguages = listOf("zh-CN", "ja", "ko"),
        outputFormat = OutputFormat.XML
    )
    
    when (result) {
        is Success -> {
            result.files.forEach { file ->
                // Copy to appropriate res/values-XX folder
            }
        }
        is Error -> {
            Log.e("ML", "Translation failed: ${result.message}")
        }
    }
}
```

---

## 12. Roadmap

### Phase 1: MVP (Q1 2026)
- [ ] Basic file upload/download
- [ ] Support for JSON, XML, .strings formats
- [ ] Translation for 10 core languages
- [ ] Web dashboard

### Phase 2: Enhanced Features (Q2 2026)
- [ ] Translation memory
- [ ] Batch processing
- [ ] API access
- [ ] Team collaboration

### Phase 3: Enterprise (Q3 2026)
- [ ] Custom glossaries
- [ ] Workflow automation
- [ ] Advanced analytics
- [ ] On-premise deployment option

### Phase 4: AI Enhancement (Q4 2026)
- [ ] Context-aware translations
- [ ] Automatic quality scoring
- [ ] Suggestion engine
- [ ] Voice/Audio localization

---

## 13. Appendix

### A. Glossary

| Term | Definition |
|------|------------|
| **i18n** | Internationalization - designing software for multiple languages |
| **l10n** | Localization - adapting software for specific locales |
| **TM** | Translation Memory - database of previously translated segments |
| **XLIFF** | XML Localization Interchange File Format |
| **ARB** | Application Resource Bundle (Flutter) |

### B. References

- [Android Localization Guide](https://developer.android.com/guide/topics/resources/localization)
- [iOS Localization Guide](https://developer.apple.com/documentation/xcode/localization)
- [Flutter Internationalization](https://docs.flutter.dev/accessibility-and-localization/internationalization)
- [XLIFF 2.0 Specification](http://docs.oasis-open.org/xliff/xliff-core/v2.0/xliff-core-v2.0.html)

---

**Document Version:** 1.0  
**Last Updated:** December 18, 2025  
**Author:** System Design Team
