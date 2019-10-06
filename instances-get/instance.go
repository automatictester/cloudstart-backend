package main

import "fmt"

type instance struct {
	InstanceID   string `json:"instanceId"`
	InstanceType string `json:"instanceType"`
	State        string `json:"state"`
	Name         string `json:"name"`
}

func (i instance) String() string {
	return fmt.Sprintf("instanceId: %s, instanceType: %s, state: %s, name: %s",
		i.InstanceID,
		i.InstanceType,
		i.State,
		i.Name)
}
