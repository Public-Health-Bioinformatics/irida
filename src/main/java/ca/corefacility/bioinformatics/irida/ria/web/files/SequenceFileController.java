package ca.corefacility.bioinformatics.irida.ria.web.files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.format.Formatter;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import ca.corefacility.bioinformatics.irida.model.SequenceFile;
import ca.corefacility.bioinformatics.irida.model.workflow.analysis.AnalysisFastQC;
import ca.corefacility.bioinformatics.irida.service.AnalysisService;
import ca.corefacility.bioinformatics.irida.service.SequenceFileService;

/**
 * Controller for all {@link SequenceFile} related views
 *
 * @author Josh Adam <josh.adam@phac-aspc.gc.ca>
 */
@Controller
@RequestMapping("/sequenceFiles")
public class SequenceFileController {
	/*
	 * PAGES
	 */
	public static final String BASE_URL = "sequenceFiles/";
	public static final String FILE_DETAIL_PAGE = BASE_URL + "file_details";
	public static final String FILE_OVERREPRESENTED = BASE_URL + "file_overrepresented";
	private static final Logger logger = LoggerFactory.getLogger(SequenceFileController.class);
	/*
	 * SUB NAV
	 */
	public static final String ACTIVE_NAV = "activeNav";
	private static final String ACTIVE_NAV_DASHBOARD = "dashboard";
	private static final String ACTIVE_NAV_OVERREPRESENTED = "overrepresented";
	/*
	 * CONVERSIONS
	 */
	Formatter<Date> dateFormatter;
	/*
	 * SERVICES
	 */
	private SequenceFileService sequenceFileService;
	private AnalysisService analysisService;

	@Autowired
	public SequenceFileController(SequenceFileService sequenceFileService, AnalysisService analysisService) {
		this.sequenceFileService = sequenceFileService;
		this.analysisService = analysisService;
		this.dateFormatter = new DateFormatter();
	}

	@RequestMapping("/{sequenceFileId}")
	public String getSequenceFilePage(final Model model, @PathVariable Long sequenceFileId) {
		logger.debug("Loading sequence files page for id: " + sequenceFileId);
		createDefaultPageInfo(sequenceFileId, model);
		model.addAttribute("perbase", "/sequenceFiles/img/" + sequenceFileId + "-perbase.png");
		model.addAttribute("persequence", "/sequenceFiles/img/" + sequenceFileId + "-persequence.png");
		model.addAttribute("dublicationlevel", "/sequenceFiles/img/" + sequenceFileId + "-dublicationlevel.png");
		model.addAttribute(ACTIVE_NAV, ACTIVE_NAV_DASHBOARD);
		return FILE_DETAIL_PAGE;
	}

	@RequestMapping("/{sequenceFileId}/overrepresented")
	public String getSequenceFileOverrepresentedPage(final Model model, @PathVariable Long sequenceFileId) {
		logger.debug("Loading sequence files page for id: " + sequenceFileId);
		createDefaultPageInfo(sequenceFileId, model);
		model.addAttribute(ACTIVE_NAV, ACTIVE_NAV_OVERREPRESENTED);
		return FILE_OVERREPRESENTED;
	}

	@RequestMapping("/download/{sequenceFileId}")
	public void downloadSequenceFile(@PathVariable Long sequenceFileId, HttpServletResponse response) throws IOException {
		SequenceFile sequenceFile = sequenceFileService.read(sequenceFileId);
		Path path = sequenceFile.getFile();
		response.setHeader("Content-Disposition", "attachment; filename=\"" + sequenceFile.getLabel() + "\"");
		Files.copy(path, response.getOutputStream());
		response.flushBuffer();
	}

	@RequestMapping(value = "/img/{sequenceFileId}-{type}", produces = MediaType.IMAGE_PNG_VALUE)
	public void downloadSequenceFileImages(@PathVariable Long sequenceFileId, @PathVariable String type, HttpServletResponse response) throws IOException {
		SequenceFile file = sequenceFileService.read(sequenceFileId);
		AnalysisFastQC fastQC = getFastQCAnalysis(file);
		if (fastQC != null) {
			byte[] chart;
			if (type.equals("perbase")) {
				chart = fastQC.getPerBaseQualityScoreChart();
			} else if (type.equals("persequence")) {
				chart = fastQC.getPerSequenceQualityScoreChart();
			} else {
				chart = fastQC.getDuplicationLevelChart();
			}
			response.getOutputStream().write(chart);
		}
		response.flushBuffer();
	}

	private AnalysisFastQC getFastQCAnalysis(SequenceFile file) {
		AnalysisFastQC analysisFastQC = null;
		Set<AnalysisFastQC> analysis = analysisService.getAnalysesForSequenceFile(file, AnalysisFastQC.class);
		if (analysis.size() > 0) {
			analysisFastQC = analysis.iterator().next();
		}
		return analysisFastQC;
	}

	private void createDefaultPageInfo(Long sequenceFileId, Model model) {
		SequenceFile file = sequenceFileService.read(sequenceFileId);
		AnalysisFastQC fastQC = getFastQCAnalysis(file);
		model.addAttribute("file", file);
		model.addAttribute("created", dateFormatter.print(file.getTimestamp(), LocaleContextHolder.getLocale()));
		model.addAttribute("fastQC", fastQC);
	}
}
