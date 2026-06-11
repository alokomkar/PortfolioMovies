# Security & Compliance Guardrails

This document establishes coding standards, configurations, and verification steps to protect customer data and application integrity in **PortfolioMovies**.

---

## 1. Advanced R8/ProGuard Optimization

To prevent reverse engineering and minimize our APK size, we enable full obfuscation, optimization, and shrinking in release builds.

### Recommended Release Configuration
Update [app/build.gradle.kts](file:///Users/alokgudikote/AndroidStudioProjects/AI/PortfolioMovies/app/build.gradle.kts) to enable shrinking:

```kotlin
buildTypes {
    release {
        isMinifyEnabled = true
        isShrinkResources = true // Requires isMinifyEnabled = true
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }
}
```

### Writing Custom Keep Rules
Avoid broad keep-all rules (e.g. `-keep class com.sortedqueue.portfolio.model.** { *; }`). Instead, specify target points for class files parsed by reflection (like Gson/Retrofit models) using specific annotations:

```proguard
# Keep reflection targets for Gson deserialization
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Preserve line numbers and source file names for production crash trace deobfuscation
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
```

---

## 2. Network Security Config & Certificate Pinning

### Long-Term Vision
We explicitly declare our network security requirements in an XML resource file to restrict cleartext HTTP traffic, specify trust anchors, and implement certificate pinning for protection against Man-in-the-Middle (MitM) attacks.

### Implementation Blueprint
1. Create `app/src/main/res/xml/network_security_config.xml`:
   ```xml
   <?xml version="1.0" encoding="utf-8"?>
   <network-security-config>
       <!-- Enforce HTTPS across the entire app -->
       <base-config cleartextTrafficPermitted="false">
           <trust-anchors>
               <certificates src="system" />
           </trust-anchors>
       </base-config>

       <!-- Enforce Certificate Pinning for TMDB API -->
       <domain-config>
           <domain includeSubdomains="true">api.themoviedb.org</domain>
           <pin-set expiration="2027-01-01">
               <!-- Primary Pin -->
               <pin digest="SHA-256">90bf631302e53b47cf296c09b8d4e929f9e160e1d09618b76b25aa0086c8cd3f</pin>
               <!-- Backup Pin -->
               <pin digest="SHA-256">85bb321302e53b47cf296c09b8d4e929f9e160e1d09618b76b25aa0086c8cd2a</pin>
           </pin-set>
       </domain-config>

       <!-- Allow cleartext in debug build for local proxy tools (e.g. Chucker/Flipper) -->
       <debug-overrides>
           <trust-anchors>
               <certificates src="user" />
           </trust-anchors>
       </debug-overrides>
   </network-security-config>
   ```
2. Reference the config in `app/src/main/AndroidManifest.xml`:
   ```xml
   <application
       android:networkSecurityConfig="@xml/network_security_config"
       ... >
   ```

---

## 3. Dependency Vulnerability Scanning

### Long-Term Vision
Third-party libraries can contain severe vulnerabilities (CVEs) that expose the application to compromise. We scan all resolved libraries in the build path during CI checks.

### Tooling Strategy
*   **OWASP Dependency-Check**: Integrates locally as a Gradle task to identify outdated dependencies with active vulnerability reports.
*   **Snyk / GitHub Dependency Review**: Automatically scans dependencies on pull requests before code reviews begin.

### Integration in CI (`.github/workflows/android-ci.yml`)
Add a scanning step in the build action:
```yaml
      - name: Run Snyk to check for vulnerabilities
        uses: snyk/actions/node@master
        env:
          SNYK_TOKEN: ${{ secrets.SNYK_TOKEN }}
        with:
          args: --severity-threshold=high
```
This blocks PR merges if a package with a known high-severity CVE is added.
