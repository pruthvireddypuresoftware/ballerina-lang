package org.wso2.ballerinalang.compiler.packaging.repo;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.resolve.PathConverter;

import java.nio.file.Path;

/**
 * Calculate bal files' path pattens in a project, excluding test files.
 */
public class ProjectSourceRepo extends NonSysRepo<Path> {

    public ProjectSourceRepo(PathConverter resolver) {
        super(resolver);
    }

    public ProjectSourceRepo(Path projectRoot) {
        this(new PathConverter(projectRoot));
    }

    @Override
    public Patten calculateNonSysPkg(PackageID pkg) {
        return new Patten(Patten.path(pkg.getName().value),
                          Patten.WILDCARD_SOURCE);
    }
}
