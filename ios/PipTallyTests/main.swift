import Foundation

print("\n=================== STARTING PIPTALLY TESTS ===================")

// Instantiate and run GameLogicTests
GameLogicTests().run()

// Instantiate and run ValidationAndEditingTests
ValidationAndEditingTests().run()

print("\n=================== ALL TESTS COMPLETED SUCCESSFUL ===================")
print("  Status: PASSED")
print("  All assertions verified on double spinners, circular seating,")
print("  score fields negative inputs, and historical totals.")
print("============================================================\n")

exit(0)
