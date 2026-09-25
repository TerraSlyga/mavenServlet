package sumdu.edu.ua.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.testng.annotations.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

public class CoreArchitectureTest {

    private final JavaClasses classes = new ClassFileImporter().importPackages("sumdu.edu.ua.core");

    @Test
    void coreShouldNotDependOnWebOrPersistence() {
        noClasses().that().resideInAPackage("..core..")
                .should().dependOnClassesThat().resideInAnyPackage("..web..", "..persistence..")
                .check(classes);
    }

    @Test
    void coreShouldNotDependOnInfrastructureFrameworks() {
        noClasses().that().resideInAPackage("..core..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "jakarta.servlet..",
                        "javax.servlet..",
                        "java.sql..",
                        "org.h2.."
                )
                .check(classes);
    }
}