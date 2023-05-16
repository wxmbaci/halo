package run.halo.app.controller.content.auth;

import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import run.halo.app.cache.AbstractStringCacheStore;
import run.halo.app.model.enums.EncryptTypeEnum;
import run.halo.app.model.properties.SheetProperties;
import run.halo.app.service.OptionService;

/**
 * Authentication for Journal.
 *
 * @author wxm
 * @date 2023-05-16
 */
@Component
public class JournalAuthentication implements ContentAuthentication {

    private final OptionService optionService;

    private final AbstractStringCacheStore cacheStore;

    public JournalAuthentication(OptionService optionService, AbstractStringCacheStore cacheStore) {
        this.cacheStore = cacheStore;
        this.optionService = optionService;

    }

    @Override
    public Object getPrincipal() {
        return EncryptTypeEnum.JOIRNALS.getName();
    }

    public String getJournalPwd() {
        String pwd = optionService.getByPropertyOrDefault(SheetProperties.JOURNALS_PASSWORD, String.class,
                SheetProperties.JOURNALS_PASSWORD.defaultValue());
        return pwd;
    }

    @Override
    public boolean isAuthenticated(Integer resourceId) {

        Boolean needCheck = optionService.getByPropertyOrDefault(SheetProperties.JOURNALS_NEED_PWD,
                Boolean.class, Boolean.valueOf(SheetProperties.JOURNALS_NEED_PWD.defaultValue()));
        if (!needCheck) {
            return true;
        }

        String pwd = optionService.getByPropertyOrDefault(SheetProperties.JOURNALS_PASSWORD, String.class,
                SheetProperties.JOURNALS_PASSWORD.defaultValue());

        if (StringUtils.isBlank(pwd)) {
            return false;
        }

        String sessionId = getSessionId();
        // No session is represent a client request
        if (StringUtils.isBlank(sessionId)) {
            return false;
        }

        String cacheKey = buildCacheKey(sessionId, getPrincipal().toString(),
                String.valueOf(resourceId));
        return cacheStore.get(cacheKey).isPresent();
    }

    @Override
    public void setAuthenticated(Integer resourceId, boolean isAuthenticated) {
        String sessionId = getSessionId();
        // No session is represent a client request
        if (StringUtils.isEmpty(sessionId)) {
            return;
        }

        String cacheKey = buildCacheKey(sessionId, getPrincipal().toString(), String.valueOf(resourceId));
        if (isAuthenticated) {
            cacheStore.putAny(cacheKey, StringUtils.EMPTY, 1, TimeUnit.DAYS);
            return;
        }
        cacheStore.delete(cacheKey);
    }

    @Override
    public void clearByResourceId(Integer resourceId) {
        String sessionId = getSessionId();
        if (StringUtils.isBlank(sessionId)) {
            return;
        }
        String cacheKey = buildCacheKey(sessionId, getPrincipal().toString(), String.valueOf(resourceId));
        // clean category cache
        cacheStore.delete(cacheKey);
    }
}
