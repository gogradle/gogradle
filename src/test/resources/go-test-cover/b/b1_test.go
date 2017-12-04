package b 

import "testing"

func Test_B1_1(t *testing.T){
    if B1(1) {
         t.Log("Passed")
    } else {
         t.Error("Failed")
    }
}

