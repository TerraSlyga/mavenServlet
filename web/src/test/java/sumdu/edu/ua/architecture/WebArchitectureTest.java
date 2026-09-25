package sumdu.edu.ua.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

public class WebArchitectureTest {

    @Test
    void webShouldNotDependOnPersistence() {
        JavaClasses imported = new ClassFileImporter()
                .importPackages("sumdu.edu.ua.web");

        noClasses()
                .that().resideInAPackage("..web..")
                .should().dependOnClassesThat()
                .resideInAPackage("..persistence..")
                .check(imported);
    }
}
