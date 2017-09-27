package ca.corefacility.bioinformatics.irida.service.impl.analysis.sample;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Collection;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import ca.corefacility.bioinformatics.irida.model.assembly.GenomeAssembly;
import ca.corefacility.bioinformatics.irida.model.assembly.GenomeAssemblyFromAnalysis;
import ca.corefacility.bioinformatics.irida.model.enums.AnalysisType;
import ca.corefacility.bioinformatics.irida.model.joins.impl.SampleGenomeAssemblyJoin;
import ca.corefacility.bioinformatics.irida.model.sample.Sample;
import ca.corefacility.bioinformatics.irida.model.workflow.submission.AnalysisSubmission;
import ca.corefacility.bioinformatics.irida.repositories.analysis.submission.AnalysisSubmissionRepository;
import ca.corefacility.bioinformatics.irida.repositories.joins.sample.GenomeAssemblyRepository;
import ca.corefacility.bioinformatics.irida.repositories.joins.sample.SampleGenomeAssemblyJoinRepository;
import ca.corefacility.bioinformatics.irida.service.analysis.sample.AnalysisSampleUpdatorService;

/**
 * Updates a sample with the results from a genome assembly.
 */
@Service
public class AssemblySampleUpdatorService implements AnalysisSampleUpdatorService {

	private static final Logger logger = LoggerFactory.getLogger(AssemblySampleUpdatorService.class);

	private final AnalysisSubmissionRepository analysisSubmissionRepository;
	private final GenomeAssemblyRepository genomeAssemblyRepository;
	private final SampleGenomeAssemblyJoinRepository sampleGenomeAssemblyJoinRepository;

	@Autowired
	public AssemblySampleUpdatorService(AnalysisSubmissionRepository analysisSubmissionRepository, 
			GenomeAssemblyRepository genomeAssemblyRepository,
			SampleGenomeAssemblyJoinRepository sampleGenomeAssemblyJoinRepository) {
		this.analysisSubmissionRepository = analysisSubmissionRepository;
		this.sampleGenomeAssemblyJoinRepository = sampleGenomeAssemblyJoinRepository;
		this.genomeAssemblyRepository = genomeAssemblyRepository;
	}

	@Override
	@Transactional
	@PreAuthorize("hasPermission(#samples, 'canUpdateSample') AND hasPermission(#analysis, 'canReadAnalysisSubmission')")
	public void update(Collection<Sample> samples, AnalysisSubmission analysis) {
		checkArgument(samples.size() == 1, "Error: expected only 1 sample, but got " + samples.size() + " samples");
		analysis = analysisSubmissionRepository.findOne(analysis.getId()); // re-load to make sure submission is re-attached to session

		Sample sample = samples.iterator().next();

		GenomeAssembly genomeAssembly = genomeAssemblyRepository.save(new GenomeAssemblyFromAnalysis(analysis));
		SampleGenomeAssemblyJoin sampleGenomeAssemblyJoin = new SampleGenomeAssemblyJoin(sample, genomeAssembly);

		logger.trace(
				"Saving join for sample [" + sample.getId() + "] to analysis submission [" + analysis.getId() + "]");
		sampleGenomeAssemblyJoinRepository.save(sampleGenomeAssemblyJoin);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AnalysisType getAnalysisType() {
		return AnalysisType.ASSEMBLY_ANNOTATION;
	}
}