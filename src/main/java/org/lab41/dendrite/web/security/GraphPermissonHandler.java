package org.lab41.dendrite.web.security;

import org.lab41.dendrite.metagraph.MetaGraph;
import org.lab41.dendrite.metagraph.MetaGraphTx;
import org.lab41.dendrite.metagraph.models.BranchMetadata;
import org.lab41.dendrite.metagraph.models.ProjectMetadata;
import org.lab41.dendrite.metagraph.models.UserMetadata;
import org.lab41.dendrite.services.MetaGraphService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.io.Serializable;

/**
 * Created by kramachandran on 8/4/14.
 */
@Service
public class GraphPermissonHandler implements PermissionEvaluator{
    Logger logger = LoggerFactory.getLogger("GraphPermissionHandler");

    @Autowired
    MetaGraphService metaGraphService;

    //TODO: Figure out this should be done as a try/finally

    boolean checkUserForBranch(Authentication authentication, Serializable targetId)
    {
        MetaGraphTx tx = metaGraphService.buildTransaction().readOnly().start();
        BranchMetadata branchMetadata = tx.getBranch((String)targetId);
        ProjectMetadata projectMetadata = branchMetadata.getProject();
        if (checkAuthAgainsUserOfProject(authentication, projectMetadata))
            return true;
        tx.commit();
       return false ;
    }

    private boolean checkAuthAgainsUserOfProject(Authentication authentication, ProjectMetadata projectMetadata) {
        for(UserMetadata user : projectMetadata.getUsers())
        {
            if(user.getName().equals(authentication.getName()))
                return true;
        }
        return false;
    }

    boolean checkUserForProject(Authentication authentication, Serializable targetId)
    {
        MetaGraphTx tx = metaGraphService.buildTransaction().readOnly().start();
        ProjectMetadata projectMetadata = tx.getProject((String)targetId);
        if (checkAuthAgainsUserOfProject(authentication, projectMetadata))
            return true;
        tx.commit();
        return false;
    }

    boolean checkUserForGraph(Authentication authentication, Serializable targetId)
    {
        MetaGraphTx tx = metaGraphService.buildTransaction().readOnly().start();
        ProjectMetadata projectMetadata = tx.getGraph((String)targetId).getProject();
        if (checkAuthAgainsUserOfProject(authentication, projectMetadata))
            return true;
        tx.commit();
        return false;
    }

    boolean checkUserForJob(Authentication authentication, Serializable targetId)
    {
        MetaGraphTx tx = metaGraphService.buildTransaction().readOnly().start();
        ProjectMetadata projectMetadata = tx.getJob((String) targetId).getProject();
        if (checkAuthAgainsUserOfProject(authentication, projectMetadata))
            return true;
        tx.commit();
        return false;

    }

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        //TODO fill this out
        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        boolean  retVal= false;
        switch(targetType) {
            case "project":
                retVal = checkUserForProject(authentication, targetId);
                break;
            case "graph":
                retVal = checkUserForGraph(authentication, targetId);
                break;
            case "branch":
                retVal = checkUserForBranch(authentication, targetId);
                break;
            case "job":
                retVal = checkUserForJob(authentication, targetId);
                break;
            default:
                retVal = false;
        }
        return retVal;
    }
}
