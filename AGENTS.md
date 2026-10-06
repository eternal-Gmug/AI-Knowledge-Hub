AGENTS.md - Guidance for AI coding agents

Checklist (follow before making edits)

- Read this file in full.
- Run a local build and tests before proposing changes.
- Only change files that are necessary for the task (minimal, surgical edits).
- When referring to project files, use the exact paths shown in this repository.

Project snapshot

- Language: Java 17 (declared via `<java.version>17</java.version>` in `pom.xml`).
- Framework: Spring Boot 4.1.1 (`spring-boot-starter-parent` in `pom.xml`).
- Main class: `src/main/java/com/scut/industrial_software/aiknowledgehub/AiKnowledgeHubApplication.java`.
- Config: `src/main/resources/application.properties` (minimal at present).
- Build: Maven wrapper available (`mvnw`, `mvnw.cmd`, `pom.xml`).

What to update here

- This file documents only discoverable, concrete conventions and workflows used by this project. Do not add generic
  Java/Spring advice.

Build / run / test (Windows PowerShell)

- Build a local jar (runs compilation and tests):

  .\mvnw.cmd clean package

- Run the application locally (uses the Spring Boot plugin):

  .\mvnw.cmd spring-boot:run

- Run tests:

  .\mvnw.cmd test

- If you prefer the platform toolchain, Maven is already configured to use Java 17 via `<java.version>17</java.version>`
  in `pom.xml`.

Project layout and where to make changes

- Spring Boot entrypoint: `src/main/java/com/scut/industrial_software/aiknowledgehub/AiKnowledgeHubApplication.java`.
    - Keep this class minimal. Add configuration classes in the same package or subpackages.
- Controllers: add REST controllers under `src/main/java/com/scut/industrial_software/aiknowledgehub/controller` (create
  this package when needed).
    - Use `@RestController` and map endpoints with `@RequestMapping` or `@GetMapping`.
    - Example path: `com.scut.industrial_software.aiknowledgehub.controller.HealthController`.
- Services: add business logic under `.../service` packages and annotate with `@Service`.
- Repositories: if JPA or other persistence is added, place under `.../repository`.
- DTOs and domain models: place under `.../model` or `.../dto` as appropriate.
- Resources: static files belong in `src/main/resources/static`, server-side templates in
  `src/main/resources/templates`.

Iteration delivery workflow

- 新的迭代/功能需求遵循四文档方法，定义见 `.github/skills/iteration-delivery/SKILL.md`：
  Requirements → Design → Development → Testing。
- 模板位于 `.github/skills/iteration-delivery/templates/`，每个迭代任务在 `docs/` 下建一个独立文件夹，
  内含 `requirements.md`、`design.md`、`development.md`、`testing.md` 四个文件。
- 文件夹命名统一为 `<迭代名>`（短横线小写，如 `initial-mvp`），四个文件名为固定的
  `requirements.md` / `design.md` / `development.md` / `testing.md`。

Dependency and plugin changes

- Add dependencies in `pom.xml`. Keep Spring dependency versions aligned with `spring-boot-starter-parent` (managed via
  Spring Boot BOM); third-party BOMs (e.g. `spring-ai-bom`) go in `<dependencyManagement>`.
- Example: to add Spring Data JPA and H2 for local development:

  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
  </dependency>
  <dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
  </dependency>

- After editing `pom.xml`, run `.\mvnw.cmd clean package` to validate.

Tests and CI expectations

- Project uses JUnit Platform (managed by `spring-boot-starter-parent` + `junit-platform-launcher` in `pom.xml`).
- Test placement: `src/test/java/com/scut/industrial_software/aiknowledgehub`.
- Keep the default `AiKnowledgeHubApplicationTests` (loads context). For fast feedback, add sliced tests (e.g.,
  `@WebMvcTest`, `@DataJpaTest`) where appropriate.

Code style and conventions (project-specific)

- Package root: `com.scut.industrial_software.aiknowledgehub` — keep new classes under this root.
- Java language level: 17. Use records or sealed types only if they add clear benefit and are compatible with the Spring
  version.
- Class naming: typical Spring conventions (Controller, Service, Repository suffixes).
- Keep `AiKnowledgeHubApplication` minimal and avoid adding business logic to the main class.

Debugging locally

- IDE: open the project as a Maven project (IntelliJ recommended). Use the Maven tool window or run configurations for
  `spring-boot:run`.
- To run from command line with remote debugging enabled:

  .\mvnw.cmd spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"

Architecture notes (as discovered)

- Currently this repository is a thin Spring Boot application with no persistence or external integrations checked in.
- The `pom.xml` includes `spring-boot-starter-restclient` and `spring-boot-starter-webmvc` — code should prefer
  using Spring's webmvc controllers and the RestClient support in the project when making HTTP calls.

What an AI agent should do when making edits

- Follow the checklist at the top of this file.
- Make minimal changes; prefer adding new classes over large refactors unless requested.
- Run `.\mvnw.cmd test` after edits and attach test output if proposing changes that affect behavior.
- When creating new packages, ensure package declarations match the directory path (
  `com.scut.industrial_software.aiknowledgehub...`).
- When modifying `pom.xml`, do not change the Spring Boot parent version without explicit need — call out the
  rationale in commit messages.

Examples referencing this codebase

- Main class: `src/main/java/com/scut/industrial_software/aiknowledgehub/AiKnowledgeHubApplication.java` (entrypoint).
- Application properties: `src/main/resources/application.properties` (contains
  `spring.application.name=AI-Knowledge-Hub`).
- Build file: `pom.xml` (shows Java version and Spring Boot parent version).

Contact and PR process

- Create small, focused commits. PR descriptions should include local build and test results (Maven output) and a
  one-line rationale.

If `AGENTS.md` did not exist

- This file was created because the repository had no existing `AGENTS.md`. If a project-specific AGENTS.md existed
  elsewhere, prefer that as the source of truth and merge only discoverable differences here.

