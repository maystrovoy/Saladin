package com.maystrovoy.dao;

import com.maystrovoy.model.Queue;
import com.maystrovoy.model.SapLog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Repository
@Transactional
public class QueueDAO {

    private static final Logger LOGGER = LogManager.getLogger(QueueDAO.class);

    @PersistenceContext
    private EntityManager entityManager;

    public boolean removeQueueFromSapLog(SapLog sapLog) {
        sapLog = getSapLog(sapLog);
        if (sapLog != null) {
            entityManager.remove(sapLog);
            LOGGER.info("remove from SapLog objectid : " + sapLog.getTargetObject());
            return true;
        } else {
            LOGGER.error("Removing failed, SAP_LOG object is empty");
            return false;
        }
    }

    private SapLog getSapLog(SapLog sapLog) {
        Query findSapLog = entityManager.createQuery("select q from SapLog q where q.targetObject = :activeParameter");
        findSapLog.setParameter("activeParameter", sapLog.getTargetObject());
        if (findSapLog.getResultList().size() > 0) {
            sapLog = (SapLog) findSapLog.getSingleResult();
        } else {
            sapLog = null;
        }
        return sapLog;
    }

    public void addQueue(Queue queue) {
        if (queue != null) {
            entityManager.persist(queue);
            LOGGER.info("add into Queue objectid : " + queue.getTargetObject() + "," + " objecttype : "
                    + queue.getObjectType());
        } else {
            LOGGER.error("Adding failed, QUEUE is empty");
        }
    }

    public List<Queue> getActiveQueue() {
        Query getActiveQueueQuery = entityManager.createQuery("select q from Queue q where q.status = :activeParameter");
        getActiveQueueQuery.setParameter("activeParameter", "ACTIVE");
        List<Queue> activeQueueList = getActiveQueueQuery.getResultList();
        return activeQueueList;
    }

}
