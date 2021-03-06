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

package me.carc.intervaltimer.model.program;

import java.util.ArrayList;
import java.util.List;

public class WorkoutItem extends DatabaseModel{
	private int totalReps;
	private Exercise attachedExercise;
	private WorkoutItem parent;

	private List<WorkoutItem> children;

	public WorkoutItem() {
		this(1);
	}

	public WorkoutItem(int repCount) {
		super();

		setTotalReps(repCount);
	}

	public WorkoutItem addChildNode(int repCount) {
		WorkoutItem newNode = new WorkoutItem(repCount);

		addChildNode(newNode);

		return newNode;
	}

	public void addChildNode(WorkoutItem node) {
		addChildNode(node, getChildren().size());
	}

	public void addChildNode(WorkoutItem node, int index) {
		checkCanHaveChildren();

		getChildren().add(index, node);
		node.setParent(this);
	}

	public void removeChild(WorkoutItem child) {
		child.setParent(null);
		getChildren().remove(child);
	}

	public Exercise addChildExercise(String name, int duration, EffortLevel effortLevel) {
		checkCanHaveChildren();

		WorkoutItem containerNode = addChildNode(1);
		Exercise newExercise = new Exercise(name, duration, effortLevel, containerNode);
		containerNode.setAttachedExercise(newExercise);

		return newExercise;
	}

	private void checkCanHaveChildren() {
		if (getAttachedExercise() != null) {
			throw new RuntimeException(
					"This ProgramNode was created with an attached exercise - it cannot have getChildren().");
		}
	}

	public boolean hasChildren() {
		return !getChildren().isEmpty();
	}

	public int getDuration() {
		if (this.getAttachedExercise() != null) {
			return getAttachedExercise().getDuration() * getTotalReps();
		} else {
			int total = 0;

			for (WorkoutItem child : getChildren()) {
				total += child.getDuration();
			}

			return total * getTotalReps();
		}
	}

	public int getTotalReps() {
		return totalReps;
	}

	public boolean isEmpty() {
		return !hasChildren() && getAttachedExercise() == null;
	}

	public Exercise getAttachedExercise() {
		return attachedExercise;
	}

	public void setTotalReps(int totalReps) {
		this.totalReps = totalReps;
	}

	public List<WorkoutItem> getChildren() {
		if (children == null) {
			// TODO
			children = new ArrayList<WorkoutItem>();
		}

		return children;
	}

	public void setAttachedExercise(Exercise attachedExercise) {
		if (attachedExercise != null) {
			attachedExercise.setNode(this);
		}
		this.attachedExercise = attachedExercise;
	}

	public WorkoutItem getParent() {
		return parent;
	}

	public void setParent(WorkoutItem parent) {
		this.parent = parent;
	}

	public int getDepth() {
		if (this.parent == null) {
			return 0;
		} else {
			return parent.getDepth() + 1;
		}
	}

	public void setChildren(List<WorkoutItem> children) {
		this.children = children;
	}

}