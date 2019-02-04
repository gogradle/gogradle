package f

import (
	"fmt"
	"testing"
)

func TestCounter_Inc(t *testing.T) {
	tests := []struct {
		ns, n, key string // Counter name and PTransform context
		inc        int64
		value      int64 // Internal variable to check
	}{
		{ns: "inc1", n: "count", key: "A", inc: 1, value: 1},
		{ns: "inc1", n: "count", key: "A", inc: 1, value: 2},
		{ns: "inc1", n: "ticker", key: "A", inc: 1, value: 1},
		{ns: "inc1", n: "ticker", key: "A", inc: 2, value: 3},
	}

	for _, test := range tests {
		t.Run(fmt.Sprintf("add %d to %s.%s[%q] value: %d", test.inc, test.ns, test.n, test.key, test.value),
			func(t *testing.T) {
			})
	}
}