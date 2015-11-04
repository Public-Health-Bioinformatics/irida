package ca.corefacility.bioinformatics.irida.model;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import ca.corefacility.bioinformatics.irida.model.enums.ExportUploadState;
import ca.corefacility.bioinformatics.irida.model.export.NcbiBioSampleFiles;
import ca.corefacility.bioinformatics.irida.model.project.Project;
import ca.corefacility.bioinformatics.irida.model.user.User;

/**
 * Class storing a request to upload sequence data to NCBI.
 * 
 * @see <a href=
 *      "http://www.ncbi.nlm.nih.gov/books/NBK47529/#_SRA_Quick_Sub_BK_Experiment_">
 *      Ncbi SRA experiment guide</a>
 */
@Entity
@Table(name = "ncbi_export_submission")
@EntityListeners(AuditingEntityListener.class)
public class NcbiExportSubmission implements MutableIridaThing {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "project_id")
	private Project project;

	@Column(name = "bio_project_id", nullable = false)
	private String bioProjectId;

	@Column(name = "namespace", nullable = false)
	private String ncbiNamespace;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinTable(name = "ncbi_export_submission_biosample")
	private List<NcbiBioSampleFiles> bioSampleFiles;

	@Column(name = "created_date", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdDate;

	@LastModifiedDate
	@Column(name = "modified_date")
	@Temporal(TemporalType.TIMESTAMP)
	private Date modifiedDate;

	@Column(name = "release_date")
	@Temporal(TemporalType.DATE)
	private Date releaseDate;

	@Column(name = "upload_state", nullable = false)
	@Enumerated(EnumType.STRING)
	private ExportUploadState uploadState;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "submitter")
	private User submitter;

	@Column(name = "directory_path")
	private String directoryPath;

	public NcbiExportSubmission() {
		uploadState = ExportUploadState.NEW;
		createdDate = new Date();
	}

	public NcbiExportSubmission(Project project, User submitter, String bioProjectId, String ncbiNamespace,
			Date releaseDate, List<NcbiBioSampleFiles> bioSampleFiles) {
		this();
		this.project = project;
		this.submitter = submitter;
		this.bioProjectId = bioProjectId;
		this.ncbiNamespace = ncbiNamespace;
		this.releaseDate = releaseDate;
		this.bioSampleFiles = bioSampleFiles;
	}

	@Override
	public Date getCreatedDate() {
		return createdDate;
	}

	@Override
	public Date getModifiedDate() {
		return modifiedDate;
	}

	@Override
	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	@Override
	public String getLabel() {
		return "NCBI Submission Project " + project.getLabel() + " " + createdDate;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public Project getProject() {
		return project;
	}

	public List<NcbiBioSampleFiles> getBioSampleFiles() {
		return bioSampleFiles;
	}

	public void setBioSampleFiles(List<NcbiBioSampleFiles> bioSampleFiles) {
		this.bioSampleFiles = bioSampleFiles;
	}

	public void setUploadState(ExportUploadState uploadState) {
		this.uploadState = uploadState;
	}

	public ExportUploadState getUploadState() {
		return uploadState;
	}

	public String getNcbiNamespace() {
		return ncbiNamespace;
	}

	public void setNcbiNamespace(String ncbiNamespace) {
		this.ncbiNamespace = ncbiNamespace;
	}

	public String getBioProjectId() {
		return bioProjectId;
	}

	public void setBioProjectId(String bioProjectId) {
		this.bioProjectId = bioProjectId;
	}

	public Date getReleaseDate() {
		return releaseDate;
	}

	public User getSubmitter() {
		return submitter;
	}

	public String getDirectoryPath() {
		return directoryPath;
	}

	public void setDirectoryPath(String directoryPath) {
		this.directoryPath = directoryPath;
	}
}
