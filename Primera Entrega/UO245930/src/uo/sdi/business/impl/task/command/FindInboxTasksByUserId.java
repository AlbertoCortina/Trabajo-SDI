package uo.sdi.business.impl.task.command;

import java.util.List;

import uo.sdi.business.exception.BusinessException;
import uo.sdi.business.impl.command.Command;
import uo.sdi.model.Task;
import uo.sdi.persistence.TaskFinder;

public class FindInboxTasksByUserId implements Command<List<Task>> {

	private Long id;

	public FindInboxTasksByUserId(Long id) {
		this.id = id;
	}

	@Override
	public List<Task> execute() throws BusinessException {
		return TaskFinder.findInboxByUserId(id);
	}
}