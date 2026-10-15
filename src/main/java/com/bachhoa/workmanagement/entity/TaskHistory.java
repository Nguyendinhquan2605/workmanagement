package com.bachhoa.workmanagement.entity;

import com.bachhoa.workmanagement.entity.enums.TaskStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "task_histories")
@Getter
@Setter
@NoArgsConstructor
public class TaskHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "task_id", nullable = false)
	private Task task;

	@Enumerated(EnumType.STRING)
	@Column(name = "old_status", length = 20)
	private TaskStatus oldStatus;

	@Enumerated(EnumType.STRING)
	@Column(name = "new_status", nullable = false, length = 20)
	private TaskStatus newStatus;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "changed_by", nullable = false)
	private User changedBy;

	@Column(name = "changed_at", nullable = false)
	private LocalDateTime changedAt;

	@jakarta.persistence.PrePersist
	protected void onCreate() {
		if (changedAt == null) {
			changedAt = LocalDateTime.now();
		}
	}
}
