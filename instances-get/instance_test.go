package main

import (
	"testing"
)

func TestString(t *testing.T) {
	exp := "instanceId: id, instanceType: type, state: state, name: name"

	i := instance{"id", "type", "state", "name"}
	got := i.String()

	if got != exp {
		t.Errorf("\nexp: %s\ngot: %s", exp, got)
	}
}
