package ca.corefacility.bioinformatics.irida.service.impl.analysis.sample;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;

import ca.corefacility.bioinformatics.irida.model.enums.AnalysisType;
import ca.corefacility.bioinformatics.irida.model.sample.Sample;
import ca.corefacility.bioinformatics.irida.model.workflow.analysis.Analysis;
import ca.corefacility.bioinformatics.irida.model.workflow.submission.AnalysisSubmission;
import ca.corefacility.bioinformatics.irida.service.analysis.sample.AnalysisSampleUpdatorService;
import ca.corefacility.bioinformatics.irida.service.analysis.sample.AnalysisSubmissionSampleProcessor;
import ca.corefacility.bioinformatics.irida.service.sample.SampleService;

/**
 * Updates samples from an {@link AnalysisSubmission} with results from the
 * analysis.
 */
@Service
public class AnalysisSubmissionSampleProcessorImpl implements AnalysisSubmissionSampleProcessor {

	private static final Logger logger = LoggerFactory.getLogger(AssemblySampleUpdatorService.class);

	private final Map<AnalysisType, AnalysisSampleUpdatorService> analysisSampleUpdatorMap;
	private final SampleService sampleService;

	/**
	 * Builds a new {@link AnalysisSubmissionSampleProcessorImpl}.
	 * 
	 * @param sampleService
	 *            The {@link SampleService}.
	 * @param analysisSampleUpdatorServices
	 *            A list of {@link AnalysisSampleUpdatorService}s to use for
	 *            updating samples.
	 */
	@Autowired
	public AnalysisSubmissionSampleProcessorImpl(SampleService sampleService,
			List<AnalysisSampleUpdatorService> analysisSampleUpdatorServices) {
		checkNotNull(analysisSampleUpdatorServices, "assemblySampleUpdatorService is null");
		this.sampleService = sampleService;
		this.analysisSampleUpdatorMap = Maps.newHashMap();

		for (AnalysisSampleUpdatorService analysisSampleUpdatorService : analysisSampleUpdatorServices) {
			AnalysisType analysisType = analysisSampleUpdatorService.getAnalysisType();
			checkArgument(!analysisSampleUpdatorMap.containsKey(analysisType),
					"Error: already have registered " + " for AnalysisType " + analysisType);

			analysisSampleUpdatorMap.put(analysisSampleUpdatorService.getAnalysisType(), analysisSampleUpdatorService);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateSamples(AnalysisSubmission analysisSubmission) {
		if (!analysisSubmission.getUpdateSamples()) {
			logger.trace("Will not update samples from results for submission=" + analysisSubmission);
		} else {
			logger.debug("Updating sample from results for submission=" + analysisSubmission);

			Collection<Sample> samples = sampleService.getSamplesForAnalysisSubimssion(analysisSubmission);
			Analysis analysis = analysisSubmission.getAnalysis();

			checkNotNull(analysis, "No analysis associated with submission " + analysisSubmission);
			checkNotNull(samples, "No samples associated with submission " + analysisSubmission);

			AnalysisSampleUpdatorService analysisSampleUpdatorService = analysisSampleUpdatorMap
					.get(analysis.getAnalysisType());

			if (analysisSampleUpdatorService != null) {
				analysisSampleUpdatorService.update(samples, analysisSubmission);
			} else {
				logger.debug(
						"No associated object for updating samples for analysis of type " + analysis.getAnalysisType());
			}
		}
	}
}