/*
 * HIIT Me, a High Intensity Interval Training app for Android
 * Copyright (C) 2015 Alex Gilleran
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.carc.intervaltimer.model;


import me.carc.intervaltimer.utils.PeekaheadLinkedList;
import me.carc.intervaltimer.utils.PeekaheadQueue;

public class WorkoutGroup extends WorkoutMetaData {

	private WorkoutItem programNode;

	public WorkoutGroup(String name) {
		this.name = name;
	}

	public WorkoutItem getAssociatedNode() {
		if (programNode == null) {
			programNode = new WorkoutItem();
		}

		return programNode;
	}

	public void setAssociatedNode(WorkoutItem programNode) {
		this.programNode = programNode;
	}

	public PeekaheadQueue<WorkoutItem> asQueue() {
		PeekaheadQueue<WorkoutItem> result = new PeekaheadLinkedList<WorkoutItem>();

		ProgramNodeState state = new ProgramNodeState(getAssociatedNode());

		while (!state.isFinished()) {
			result.add(state.getCurrentExercise().getParentNode());
			state.next();
		}

		return result;
	}
}