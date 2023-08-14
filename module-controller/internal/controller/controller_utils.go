package controller

import moduledeploymentv1alpha1 "github.com/sofastack/sofa-serverless/api/v1alpha1"

type ModuleReplicaSetsByCreationTimestamp []*moduledeploymentv1alpha1.ModuleReplicaSet

func (m ModuleReplicaSetsByCreationTimestamp) Len() int {
	return len(m)
}

func (m ModuleReplicaSetsByCreationTimestamp) Less(i, j int) bool {
	if m[i].CreationTimestamp.Equal(&m[j].CreationTimestamp) {
		return m[i].Name < m[j].Name
	}
	return m[i].CreationTimestamp.Before(&m[j].CreationTimestamp)
}

func (m ModuleReplicaSetsByCreationTimestamp) Swap(i, j int) {
	m[i], m[j] = m[j], m[i]
}
