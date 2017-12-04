package a

import "testing"

func Test_A2_1(t *testing.T){
    if !A2(1) {
         t.Log("Passed")
    } else {
         t.Error("Failed")
    }
}

