# functions operating on updateable variables

namespace eval turbine {

    # initialise updateable variable o with value in future i
    # for now, assume floats
    proc init_updateable { stack o i } {
        #TODO
    }

    proc update_min { stack o inputs } {
        set x [ lindex $inputs 0 ]
        set val [ lindex $inputs 1 ]
        rule "update_min-$x-$val" $val \
            $turbine::LOCAL "update_min_body $x $val"
    }
    proc update_min_body { x val } {
        set val2 [ get_float $val ]
        update_min_impl $x $val2
    }

    proc update_min_impl { x val } {
        # TODO: update $x with tcl value $val
    }
    
    proc update_incr { stack o inputs } {
        set x [ lindex $inputs 0 ]
        set val [ lindex $inputs 1 ]
        rule "update_incr-$x-$val" $val \
            $turbine::LOCAL "update_incr_body $x $val"
    }
    proc update_incr_body { x val } {
        set val2 [ get_float $val ]
        update_incr_impl $x $val2
    }

    proc update_incr_impl { x val } {
        # TODO: update $x with tcl value $val
    }

    proc update_scale { stack o inputs } {
        set x [ lindex $inputs 0 ]
        set val [ lindex $inputs 1 ]
        rule "update_scale-$x-$val" $val \
            $turbine::LOCAL "update_scale_body $x $val"
    }
    proc update_scale_body { x val } {
        set val2 [ get_float $val ]
        update_scale_impl $x $val2
    }

    proc update_scale_impl { x val } {
        # TODO: update $x with tcl value $val
    }
}
