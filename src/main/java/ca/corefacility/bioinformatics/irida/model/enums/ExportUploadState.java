package ca.corefacility.bioinformatics.irida.model.enums;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import ca.corefacility.bioinformatics.irida.model.NcbiExportSubmission;

/**
 * Status of an {@link NcbiExportSubmission}. Many states taken from <a href=
 * "http://www.ncbi.nlm.nih.gov/viewvc/v1/trunk/submit/public-docs/common/submission-response.xsd"
 * >upload response XSD</a>.
 */
public enum ExportUploadState {

	/**
	 * Newly created submission
	 */
	NEW("NEW"),

	/**
	 * Submission currently being uploaded
	 */
	UPLOADING("UPLOADING"),

	/**
	 * Submission which has been successfully uploaded
	 */
	UPLOADED("UPLOADED"),

	/**
	 * Submission where an error occurred while uploading
	 */
	UPLOAD_ERROR("UPLOAD_ERROR"),

	/**
	 * Submission created and being populated
	 */
	CREATED("created"),

	/**
	 * Failed immediately after submission
	 */
	FAILED("failed"),

	/**
	 * Queued for processing
	 */
	QUEUED("queued"),

	/**
	 * Processing started
	 */
	PROCESSING("processing"),

	/**
	 * Processing completed successfully
	 */
	PROCESSED_OK("processed-ok"),

	/**
	 * Processing completed with error(s)
	 */
	PROCESSED_ERROR("processed-error"),

	/**
	 * Waiting for other files to continue processing
	 */
	WAITING("waiting"),

	/**
	 * Submitted to NCBI
	 */
	SUBMITTED("submitted"),

	/**
	 * Submission deleted
	 */
	DELETED("Submission deleted"),

	/**
	 * Retried processing of failed action(s
	 */
	RETRIED("retried"),

	/**
	 * used for undefined states
	 */
	UNKNOWN("unknown");

	private static Map<String, ExportUploadState> stateMap = new HashMap<>();
	private String stateString;

	// set of statuses that should be watched and update
	private static Set<ExportUploadState> updateableStates = ImmutableSet.of(NEW, SUBMITTED, CREATED, QUEUED,
			PROCESSING, WAITING);

	static {
		for (ExportUploadState state : ExportUploadState.values()) {
			stateMap.put(state.toString(), state);
		}
	}

	private ExportUploadState(String stateString) {
		this.stateString = stateString;
	}

	/**
	 * Get an {@link ExportUploadState} from its string representation
	 * 
	 * @param stateString
	 *            The state as a String
	 * @return {@link ExportUploadState} for the given string
	 */
	public static ExportUploadState fromString(String stateString) {
		ExportUploadState state = stateMap.get(stateString);
		checkNotNull(state, "state for string \"" + stateString + "\" does not exist");

		return state;
	}

	/**
	 * Get a set of the {@link ExportUploadState}s that should be watched and
	 * updated
	 * 
	 * @return a set of {@link ExportUploadState}
	 */
	public static Set<ExportUploadState> getUpdateableStates() {
		return updateableStates;
	}

	/**
	 * Return the String representation of the {@link ExportUploadState}
	 */
	@Override
	public String toString() {
		return stateString;
	}
}
