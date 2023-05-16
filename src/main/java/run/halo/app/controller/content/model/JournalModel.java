package run.halo.app.controller.content.model;

import static org.springframework.data.domain.Sort.Direction.DESC;
import static run.halo.app.model.support.HaloConst.POST_PASSWORD_TEMPLATE;
import static run.halo.app.model.support.HaloConst.SUFFIX_FTL;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import run.halo.app.controller.content.auth.JournalAuthentication;
import run.halo.app.model.entity.Journal;
import run.halo.app.model.enums.JournalType;
import run.halo.app.model.properties.SheetProperties;
import run.halo.app.service.JournalService;
import run.halo.app.service.OptionService;
import run.halo.app.service.ThemeService;

/**
 * @author ryanwang
 * @date 2020-02-11
 */
@Component
public class JournalModel {

    private final JournalService journalService;

    private final OptionService optionService;

    private final ThemeService themeService;

    private JournalAuthentication journalAuthentication;

    public JournalModel(JournalService journalService,
        OptionService optionService,
            ThemeService themeService,
            JournalAuthentication journalAuthentication) {
        this.journalService = journalService;
        this.optionService = optionService;
        this.themeService = themeService;
        this.journalAuthentication = journalAuthentication;
    }

    public String list(Integer page, Model model) {

        if (!journalAuthentication.isAuthenticated(0)) {
            model.addAttribute("slug", "list");
            model.addAttribute("type", "journals");
            if (themeService.templateExists(POST_PASSWORD_TEMPLATE + SUFFIX_FTL)) {
                return themeService.render(POST_PASSWORD_TEMPLATE);
            }
            return "common/template/" + POST_PASSWORD_TEMPLATE;
        }

        int pageSize = optionService
            .getByPropertyOrDefault(SheetProperties.JOURNALS_PAGE_SIZE, Integer.class,
                Integer.parseInt(SheetProperties.JOURNALS_PAGE_SIZE.defaultValue()));

        Pageable pageable =
            PageRequest.of(page >= 1 ? page - 1 : page, pageSize, Sort.by(DESC, "createTime"));

        Page<Journal> journals = journalService.pageBy(JournalType.PUBLIC, pageable);

        model.addAttribute("is_journals", true);
        model.addAttribute("journals", journalService.convertToCmtCountDto(journals));
        model.addAttribute("meta_keywords", optionService.getSeoKeywords());
        model.addAttribute("meta_description", optionService.getSeoDescription());
        return themeService.render("journals");
    }
}
